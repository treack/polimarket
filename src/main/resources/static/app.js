'use strict';

const ui = Object.fromEntries([
  'seller-form', 'seller-id', 'validate-button', 'seller-status', 'search',
  'refresh-button', 'catalog-status', 'product-count', 'product-list',
  'availability-panel', 'selected-product', 'availability-form', 'quantity',
  'availability-button', 'availability-status', 'add-to-order', 'orders-workspace',
  'cart-items', 'order-form', 'customer', 'delivery-address', 'create-order',
  'order-status', 'refresh-orders', 'orders-status', 'orders-list'
].map(id => [id, document.getElementById(id)]));
let authorizedSeller = null;
let products = [];
let selectedProduct = null;
let sessionVersion = 0;
let catalogVersion = 0;
let availabilityVersion = 0;
let cart = new Map();
let ordersVersion = 0;
let writing = false;
let lastCreatedOrderId = null;

function status(id, message, kind = '') {
  ui[id].textContent = message;
  ui[id].className = `status ${kind}`;
}

function positiveInteger(value) {
  const number = Number(value);
  return Number.isSafeInteger(number) && number > 0 ? number : null;
}

async function request(url, body) {
  let response;
  try { response = await fetch(url, { method: body === undefined ? 'GET' : 'POST',
    headers: { Accept: 'application/json', ...(body === undefined ? {} : { 'Content-Type': 'application/json' }) },
    ...(body === undefined ? {} : { body: JSON.stringify(body) }), cache: 'no-store' }); }
  catch { throw new Error(body === undefined ? 'No se pudo conectar con el servidor. Intenta de nuevo.'
    : 'No se pudo confirmar la operación. Actualiza los pedidos para verificar el resultado antes de volver a enviarla.'); }
  if (!response.ok) {
    let detail;
    try { detail = (await response.json()).detail; } catch { /* El servidor puede responder sin JSON. */ }
    throw new Error(detail || `No se pudo completar la consulta (HTTP ${response.status}).`);
  }
  return response.json();
}

function clearSelection() {
  selectedProduct = null;
  availabilityVersion++;
  ui['availability-panel'].hidden = true;
  ui['availability-button'].disabled = false;
  status('availability-status', '');
}

function resetSeller() {
  sessionVersion++;
  catalogVersion++;
  authorizedSeller = null;
  ordersVersion++;
  cart.clear();
  lastCreatedOrderId = null;
  ui['orders-workspace'].hidden = true;
  ui['orders-list'].replaceChildren();
  ui['order-form'].reset();
  status('order-status', '');
  renderCart();
  products = [];
  ui.search.value = '';
  ui.search.disabled = true;
  ui['refresh-button'].disabled = true;
  ui['validate-button'].disabled = false;
  ui['validate-button'].textContent = 'Validar vendedor →';
  ui['product-list'].replaceChildren();
  ui['product-count'].textContent = '—';
  clearSelection();
  status('catalog-status', 'Valida un vendedor autorizado para cargar el catálogo.');
}

function renderProducts() {
  const query = ui.search.value.trim().toLocaleLowerCase('es');
  const visible = products.filter(product => product.nombre.toLocaleLowerCase('es').includes(query)
    || String(product.id).includes(query));
  ui['product-count'].textContent = `${visible.length} producto${visible.length === 1 ? '' : 's'}`;
  ui['product-list'].replaceChildren();
  for (const product of visible) {
    const row = document.createElement('tr');
    const nameCell = document.createElement('td');
    const name = document.createElement('strong');
    name.textContent = product.nombre;
    const id = document.createElement('small');
    id.textContent = `ID ${product.id}`;
    nameCell.append(name, id);
    const stockCell = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = 'stock-badge';
    badge.textContent = `${product.existencias} unid.`;
    stockCell.append(badge);
    const actionCell = document.createElement('td');
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'choose-button';
    button.textContent = 'Consultar';
    button.setAttribute('aria-label', `Consultar disponibilidad de ${product.nombre}`);
    button.addEventListener('click', () => {
      clearSelection();
      selectedProduct = product;
      ui['selected-product'].textContent = `${product.nombre} · ID ${product.id}`;
      ui.quantity.value = '1';
      ui['availability-panel'].hidden = false;
      ui.quantity.focus();
    });
    actionCell.append(button);
    row.append(nameCell, stockCell, actionCell);
    ui['product-list'].append(row);
  }
  status('catalog-status', products.length === 0 ? 'No hay productos con existencias disponibles.'
    : visible.length === 0 ? 'No encontramos productos con esa búsqueda.'
      : 'Existencias consultadas. Actualiza el catálogo para ver cambios recientes.');
}

async function loadProducts() {
  if (authorizedSeller === null) return;
  const session = sessionVersion;
  const version = ++catalogVersion;
  clearSelection();
  ui['refresh-button'].disabled = true;
  ui.search.disabled = true;
  ui['product-list'].replaceChildren();
  status('catalog-status', 'Consultando productos…');
  try {
    const result = await request('/api/ventas/productos');
    if (session !== sessionVersion || version !== catalogVersion) return;
    if (!Array.isArray(result)) throw new Error('El servidor devolvió un catálogo inválido.');
    products = result.map(product => ({ ...product, nombre: String(product.nombre == null ? 'Sin nombre' : product.nombre) }));
    renderProducts();
  } catch (error) {
    if (session !== sessionVersion || version !== catalogVersion) return;
    products = [];
    ui['product-count'].textContent = '—';
    status('catalog-status', error.message, 'error');
  } finally {
    if (session === sessionVersion && version === catalogVersion) {
      ui['refresh-button'].disabled = false;
      ui.search.disabled = false;
    }
  }
}

ui['seller-id'].addEventListener('input', () => {
  resetSeller();
  status('seller-status', 'Pendiente de validación.');
});
ui['seller-form'].addEventListener('submit', async event => {
  event.preventDefault();
  resetSeller();
  const id = positiveInteger(ui['seller-id'].value);
  if (id === null) { status('seller-status', 'Ingresa un ID entero mayor que cero.', 'error'); return; }
  const session = sessionVersion;
  ui['validate-button'].disabled = true;
  ui['validate-button'].textContent = 'Validando…';
  status('seller-status', 'Consultando autorización…');
  try {
    const result = await request(`/api/rrhh/vendedores/${id}/autorizacion`);
    if (session !== sessionVersion) return;
    if (result.autorizado !== true) {
      status('seller-status', 'Vendedor no autorizado. Verifica el ID o consulta con Recursos Humanos.', 'error');
      return;
    }
    authorizedSeller = id;
    ui['orders-workspace'].hidden = false;
    status('seller-status', `Vendedor ${id} autorizado. Ya puedes consultar el catálogo.`, 'success');
    await Promise.all([loadProducts(), loadOrders()]);
  } catch (error) {
    if (session === sessionVersion) status('seller-status', error.message, 'error');
  } finally {
    if (session === sessionVersion) {
      ui['validate-button'].disabled = false;
      ui['validate-button'].textContent = 'Validar vendedor →';
    }
  }
});
ui.search.addEventListener('input', renderProducts);
ui['refresh-button'].addEventListener('click', loadProducts);
ui.quantity.addEventListener('input', () => {
  availabilityVersion++;
  ui['availability-button'].disabled = false;
  status('availability-status', '');
});
ui['availability-form'].addEventListener('submit', async event => {
  event.preventDefault();
  if (authorizedSeller === null || selectedProduct === null) return;
  const quantity = positiveInteger(ui.quantity.value);
  if (quantity === null || quantity > 2147483647) {
    status('availability-status', 'Ingresa una cantidad entera entre 1 y 2147483647.', 'error'); return;
  }
  const session = sessionVersion;
  const version = ++availabilityVersion;
  const product = selectedProduct;
  ui['availability-button'].disabled = true;
  status('availability-status', 'Verificando existencias…');
  try {
    const result = await request(`/api/bodega/productos/${product.id}/disponibilidad?cantidad=${quantity}`);
    if (session !== sessionVersion || version !== availabilityVersion) return;
    status('availability-status', result.disponible === true
      ? `Hay existencias suficientes para ${quantity} unidades de ${product.nombre}.`
      : `No hay existencias suficientes para ${quantity} unidades. Actualiza el catálogo y verifica otra cantidad.`,
    result.disponible === true ? 'success' : 'error');
  } catch (error) {
    if (session === sessionVersion && version === availabilityVersion) status('availability-status', error.message, 'error');
  } finally {
    if (session === sessionVersion && version === availabilityVersion) ui['availability-button'].disabled = false;
  }
});

function renderCart() {
  ui['cart-items'].replaceChildren();
  if (cart.size === 0) {
    const empty = document.createElement('p');
    empty.className = 'muted';
    empty.textContent = 'El pedido está vacío. Selecciona un producto y agrega una cantidad.';
    ui['cart-items'].append(empty);
  }
  for (const item of cart.values()) {
    const row = document.createElement('div');
    row.className = 'cart-row';
    const text = document.createElement('span');
    text.textContent = item.nombre + ' · ' + item.cantidad + ' unidades';
    const remove = document.createElement('button');
    remove.type = 'button';
    remove.className = 'secondary';
    remove.textContent = 'Quitar';
    remove.disabled = writing;
    remove.setAttribute('aria-label', 'Quitar ' + item.nombre + ' del pedido');
    remove.addEventListener('click', () => { cart.delete(item.productoId); renderCart(); });
    row.append(text, remove);
    ui['cart-items'].append(row);
  }
  ui['create-order'].disabled = writing || cart.size === 0;
}

ui['add-to-order'].addEventListener('click', () => {
  if (writing || !selectedProduct || authorizedSeller === null) return;
  const cantidad = positiveInteger(ui.quantity.value);
  const anterior = cart.get(selectedProduct.id);
  const acumulada = cantidad === null ? null : cantidad + (anterior ? anterior.cantidad : 0);
  if (acumulada === null || acumulada > 2147483647 || acumulada > selectedProduct.existencias) {
    status('availability-status', 'La cantidad total del pedido supera las existencias consultadas o no es válida.', 'error');
    return;
  }
  cart.set(selectedProduct.id, { productoId: selectedProduct.id, nombre: selectedProduct.nombre, cantidad: acumulada });
  renderCart();
  status('availability-status', 'Producto agregado al pedido. Completa los datos del cliente para crearlo.', 'success');
});

function lockWrites(locked) {
  writing = locked;
  ui['seller-id'].disabled = locked;
  ui['validate-button'].disabled = locked;
  ui['add-to-order'].disabled = locked;
  ui['refresh-orders'].disabled = locked;
  ui.customer.disabled = locked;
  ui['delivery-address'].disabled = locked;
  renderCart();
  for (const button of ui['orders-list'].querySelectorAll('button')) button.disabled = locked;
}

const money = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' });

async function loadOrders() {
  if (authorizedSeller === null) return;
  const session = sessionVersion;
  const version = ++ordersVersion;
  ui['refresh-orders'].disabled = true;
  status('orders-status', 'Consultando pedidos…');
  try {
    const orders = await request('/api/ventas/pedidos?vendedorId=' + authorizedSeller);
    if (session !== sessionVersion || version !== ordersVersion) return;
    ui['orders-list'].replaceChildren();
    for (const order of orders) {
      const article = document.createElement('article');
      article.className = 'order-item';
      const heading = document.createElement('h3');
      heading.textContent = 'Pedido #' + order.id + ' · ' + (order.estado === 'DESPACHADO' ? 'Despachado' : 'Pendiente');
      const customer = document.createElement('p');
      customer.textContent = order.cliente + ' · ' + order.direccion;
      const lines = document.createElement('ul');
      for (const item of order.detalles) {
        const line = document.createElement('li');
        line.textContent = item.nombre + ' × ' + item.cantidad + ' · ' + money.format(item.precioUnitario) + ' c/u';
        lines.append(line);
      }
      const total = document.createElement('p');
      total.className = 'order-total';
      total.textContent = 'Total: ' + money.format(order.total);
      article.append(heading, customer, lines, total);
      if (order.estado === 'PENDIENTE') {
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = 'Registrar entrega y salida';
        button.setAttribute('aria-label', 'Registrar entrega y salida del pedido ' + order.id);
        button.disabled = writing;
        button.addEventListener('click', async () => {
          if (writing) return;
          lockWrites(true);
          status('orders-status', 'Registrando entrega y salida del pedido #' + order.id + '…');
          try {
            const result = await request('/api/entregas/pedidos/' + order.id, {});
            if (lastCreatedOrderId === order.id) {
              status('order-status', 'Pedido #' + order.id + ' despachado. La entrega y la salida ya están registradas.', 'success');
            }
            await Promise.all([loadOrders(), loadProducts()]);
            status('orders-status', 'Entrega #' + result.id + ' registrada para el pedido #' + result.pedidoId
              + '. La salida quedó registrada y el inventario fue actualizado.', 'success');
          } catch (error) {
            status('orders-status', error.message, 'error');
          } finally { lockWrites(false); }
        });
        article.append(button);
      } else {
        const completed = document.createElement('p');
        completed.className = 'status success';
        completed.textContent = 'Entrega y salida registradas. Este pedido no puede despacharse de nuevo.';
        article.append(completed);
      }
      ui['orders-list'].append(article);
    }
    status('orders-status', orders.length ? 'Últimos pedidos, del más reciente al más antiguo.' : 'Todavía no hay pedidos de este vendedor.');
  } catch (error) {
    if (session === sessionVersion && version === ordersVersion) status('orders-status', error.message, 'error');
  } finally {
    if (session === sessionVersion && version === ordersVersion) ui['refresh-orders'].disabled = writing;
  }
}

ui['refresh-orders'].addEventListener('click', loadOrders);
ui['order-form'].addEventListener('submit', async event => {
  event.preventDefault();
  if (writing || authorizedSeller === null || cart.size === 0) return;
  const cliente = ui.customer.value.trim();
  const direccion = ui['delivery-address'].value.trim();
  if (!cliente || !direccion) { status('order-status', 'Completa el cliente y la dirección.', 'error'); return; }
  const detalles = Array.from(cart.values(), item => ({ productoId: item.productoId, cantidad: item.cantidad }));
  lockWrites(true);
  status('order-status', 'Creando pedido…');
  try {
    const order = await request('/api/ventas/pedidos', { vendedorId: authorizedSeller, cliente, direccion, detalles });
    lastCreatedOrderId = order.id;
    cart.clear();
    ui['order-form'].reset();
    status('order-status', 'Pedido #' + order.id + ' creado por ' + money.format(order.total)
      + '. Está pendiente de entrega y todavía no descuenta inventario.', 'success');
    await loadOrders();
  } catch (error) {
    status('order-status', error.message, 'error');
  } finally { lockWrites(false); }
});
renderCart();
