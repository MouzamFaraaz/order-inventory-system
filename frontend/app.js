// ===================================================================
// ORDER & INVENTORY MANAGEMENT SYSTEM — FRONTEND DASHBOARD
// Connects to Spring Boot Microservices via API Gateway (:8080)
// ===================================================================

const CONFIG = {
  GATEWAY_URL: '',
  AUTH_ENDPOINT: '/auth/login',
  PRODUCTS_ENDPOINT: '/api/products',
  ORDERS_ENDPOINT: '/api/orders',
  NOTIFICATIONS_ENDPOINT: '/api/notifications'
};

// Application State
let state = {
  jwtToken: localStorage.getItem('jwt_token') || null,
  currentUser: JSON.parse(localStorage.getItem('user_info')) || { username: 'Guest (Not Logged In)', role: 'ANONYMOUS', userId: null },
  products: [],
  orders: [],
  notifications: [],
  stats: {
    productsCount: 0,
    ordersCount: 0,
    confirmedCount: 0,
    eventsCount: 0
  },
  isLiveBackend: false
};

// Initial Seed Data for Demo / Fallback Mode
const DEFAULT_PRODUCTS = [
  { id: 55, name: 'Wireless Mechanical Keyboard', availableQuantity: 50, reservedQuantity: 0, updatedAt: new Date().toISOString() },
  { id: 56, name: 'Ergonomic Vertical Mouse', availableQuantity: 25, reservedQuantity: 0, updatedAt: new Date().toISOString() },
  { id: 57, name: '4K Ultra HD Gaming Monitor', availableQuantity: 10, reservedQuantity: 0, updatedAt: new Date().toISOString() },
  { id: 58, name: 'USB-C Multiport Hub (10-in-1)', availableQuantity: 2, reservedQuantity: 0, updatedAt: new Date().toISOString() },
  { id: 59, name: 'Noise Cancelling Headphones', availableQuantity: 0, reservedQuantity: 0, updatedAt: new Date().toISOString() }
];

// Initialize on DOM Load
document.addEventListener('DOMContentLoaded', () => {
  lucide.createIcons();
  updateAuthUI();
  checkBackendHealth();
  loadInitialData();

  // Auto-refresh notifications and health periodically
  setInterval(checkBackendHealth, 15000);
});

// ===================================================================
// 1. HEALTH & CONNECTIVITY
// ===================================================================
async function checkBackendHealth() {
  const dot = document.getElementById('backend-status-dot');
  const text = document.getElementById('backend-status-text');

  try {
    const res = await fetch(`${CONFIG.GATEWAY_URL}/actuator/health`, { method: 'GET', signal: AbortSignal.timeout(2000) });
    if (res.ok) {
      state.isLiveBackend = true;
      dot.className = 'w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse';
      text.innerText = 'Connected (:8080)';
      text.className = 'text-emerald-400 font-medium';
    } else {
      throw new Error('Gateway returned non-OK');
    }
  } catch (e) {
    state.isLiveBackend = false;
    dot.className = 'w-2.5 h-2.5 rounded-full bg-amber-500';
    text.innerText = 'Interactive Demo Mode';
    text.className = 'text-amber-400 font-medium';
  }
}

function getAuthHeaders() {
  const headers = { 'Content-Type': 'application/json' };
  if (state.jwtToken) {
    headers['Authorization'] = `Bearer ${state.jwtToken}`;
  }
  return headers;
}

// ===================================================================
// 2. AUTHENTICATION & LOGIN
// ===================================================================
function openLoginModal() {
  document.getElementById('modal-login').classList.remove('hidden');
}

function closeLoginModal() {
  document.getElementById('modal-login').classList.add('hidden');
}

function fillLogin(u, p) {
  document.getElementById('login-username').value = u;
  document.getElementById('login-password').value = p;
}

async function handleLoginSubmit(e) {
  e.preventDefault();
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;

  try {
    let authData;
    if (state.isLiveBackend) {
      const res = await fetch(`${CONFIG.GATEWAY_URL}${CONFIG.AUTH_ENDPOINT}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      if (!res.ok) throw new Error('Invalid credentials from Gateway');
      authData = await res.json();
    } else {
      // In-memory token generator for offline / test preview
      authData = {
        token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.demo_token_' + Date.now(),
        username: username,
        role: username === 'admin' ? 'ADMIN' : 'CUSTOMER',
        userId: username === 'admin' ? 1 : 101
      };
    }

    state.jwtToken = authData.token;
    state.currentUser = {
      username: authData.username,
      role: authData.role,
      userId: authData.userId
    };

    localStorage.setItem('jwt_token', state.jwtToken);
    localStorage.setItem('user_info', JSON.stringify(state.currentUser));

    updateAuthUI();
    closeLoginModal();
    showToast(`Logged in as ${state.currentUser.username} (${state.currentUser.role})`, 'success');
  } catch (err) {
    showToast(`Login failed: ${err.message}`, 'error');
  }
}

function updateAuthUI() {
  const display = document.getElementById('username-display');
  if (state.currentUser && state.currentUser.userId) {
    display.innerText = `${state.currentUser.username} (${state.currentUser.role})`;
  } else {
    display.innerText = 'Login (JWT)';
  }
}

// ===================================================================
// 3. TAB NAVIGATION
// ===================================================================
function switchTab(tabId) {
  ['dashboard', 'products', 'orders', 'notifications'].forEach(t => {
    const tabEl = document.getElementById(`tab-${t}`);
    const btnEl = document.getElementById(`tab-btn-${t}`);
    if (t === tabId) {
      tabEl.classList.remove('hidden');
      btnEl.className = 'nav-tab px-4 py-2 rounded-lg text-sm font-medium transition flex items-center space-x-2 text-indigo-400 bg-slate-800/80';
    } else {
      tabEl.classList.add('hidden');
      btnEl.className = 'nav-tab px-4 py-2 rounded-lg text-sm font-medium transition flex items-center space-x-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800/50';
    }
  });

  if (tabId === 'products') fetchProducts();
  if (tabId === 'orders') fetchOrders();
  if (tabId === 'notifications') fetchNotifications();
  lucide.createIcons();
}

// ===================================================================
// 4. INVENTORY & PRODUCTS
// ===================================================================
async function loadInitialData() {
  state.products = [...DEFAULT_PRODUCTS];
  renderProducts();
  populateOrderProductSelect();
  updateStats();
}

async function fetchProducts() {
  if (state.isLiveBackend) {
    try {
      const res = await fetch(`${CONFIG.GATEWAY_URL}${CONFIG.PRODUCTS_ENDPOINT}`, {
        headers: getAuthHeaders()
      });
      if (res.ok) {
        state.products = await res.json();
      }
    } catch (e) {
      console.warn('Using fallback products data');
    }
  }
  renderProducts();
  populateOrderProductSelect();
  updateStats();
}

function renderProducts() {
  const grid = document.getElementById('products-grid');
  grid.innerHTML = '';

  state.products.forEach(p => {
    const isOutOfStock = p.availableQuantity === 0;
    const isLowStock = p.availableQuantity > 0 && p.availableQuantity <= 5;
    
    let statusBadge = `<span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">In Stock (${p.availableQuantity})</span>`;
    if (isOutOfStock) {
      statusBadge = `<span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20">Out of Stock</span>`;
    } else if (isLowStock) {
      statusBadge = `<span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20">Low Stock (${p.availableQuantity})</span>`;
    }

    const card = document.createElement('div');
    card.className = 'glass-panel p-5 rounded-xl flex flex-col justify-between hover:border-slate-700 transition';
    card.innerHTML = `
      <div>
        <div class="flex items-center justify-between mb-2">
          <span class="text-xs font-mono text-slate-500">ID: #${p.id}</span>
          ${statusBadge}
        </div>
        <h4 class="font-bold text-slate-100 text-sm mb-1">${p.name}</h4>
        <div class="grid grid-cols-2 gap-2 mt-4 text-xs">
          <div class="bg-slate-900/60 p-2 rounded border border-slate-800">
            <span class="text-slate-500 block text-[10px]">Available</span>
            <span class="font-mono font-bold text-slate-200">${p.availableQuantity}</span>
          </div>
          <div class="bg-slate-900/60 p-2 rounded border border-slate-800">
            <span class="text-slate-500 block text-[10px]">Reserved</span>
            <span class="font-mono font-bold text-indigo-400">${p.reservedQuantity || 0}</span>
          </div>
        </div>
      </div>
      <div class="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between">
        <button onclick="openUpdateStockModal(${p.id}, '${p.name.replace(/'/g, "\\'")}', ${p.availableQuantity})" class="text-xs text-indigo-400 hover:text-indigo-300 font-medium flex items-center gap-1">
          <i data-lucide="edit" class="w-3 h-3"></i> Adjust Stock
        </button>
        <button onclick="quickOrderProduct(${p.id})" class="px-2.5 py-1 text-xs rounded bg-indigo-600/20 text-indigo-300 hover:bg-indigo-600 hover:text-white border border-indigo-500/30 transition">
          Quick Order
        </button>
      </div>
    `;
    grid.appendChild(card);
  });
  lucide.createIcons();
}

function populateOrderProductSelect() {
  const select = document.getElementById('order-product-select');
  if (!select) return;
  select.innerHTML = '';
  state.products.forEach(p => {
    const opt = document.createElement('option');
    opt.value = p.id;
    opt.innerText = `#${p.id} - ${p.name} (Stock: ${p.availableQuantity})`;
    select.appendChild(opt);
  });
}

function quickOrderProduct(prodId) {
  switchTab('orders');
  const select = document.getElementById('order-product-select');
  if (select) select.value = prodId;
}

// Modal Handlers for Product Management
function openAddProductModal() {
  document.getElementById('modal-product').classList.remove('hidden');
}

function closeAddProductModal() {
  document.getElementById('modal-product').classList.add('hidden');
}

async function handleAddProductSubmit(e) {
  e.preventDefault();
  const name = document.getElementById('new-prod-name').value.trim();
  const availableQuantity = parseInt(document.getElementById('new-prod-stock').value, 10);

  const payload = { name, availableQuantity, reservedQuantity: 0 };

  if (state.isLiveBackend) {
    try {
      const res = await fetch(`${CONFIG.GATEWAY_URL}${CONFIG.PRODUCTS_ENDPOINT}`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        const created = await res.json();
        state.products.push(created);
      }
    } catch (err) {
      showToast(`Error creating product: ${err.message}`, 'error');
    }
  } else {
    const newId = 60 + state.products.length;
    state.products.push({ id: newId, name, availableQuantity, reservedQuantity: 0, updatedAt: new Date().toISOString() });
  }

  closeAddProductModal();
  renderProducts();
  populateOrderProductSelect();
  showToast(`Product "${name}" added to catalog!`, 'success');
}

function openUpdateStockModal(id, name, currentQty) {
  document.getElementById('update-stock-prod-id').value = id;
  document.getElementById('update-stock-prod-name').innerText = `#${id} ${name}`;
  document.getElementById('update-stock-qty').value = currentQty;
  document.getElementById('modal-stock').classList.remove('hidden');
}

function closeUpdateStockModal() {
  document.getElementById('modal-stock').classList.add('hidden');
}

async function handleUpdateStockSubmit(e) {
  e.preventDefault();
  const id = parseInt(document.getElementById('update-stock-prod-id').value, 10);
  const qty = parseInt(document.getElementById('update-stock-qty').value, 10);

  if (state.isLiveBackend) {
    try {
      await fetch(`${CONFIG.GATEWAY_URL}${CONFIG.PRODUCTS_ENDPOINT}/${id}/stock`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({ availableQuantity: qty })
      });
    } catch (err) {
      showToast(`Failed to update stock: ${err.message}`, 'error');
    }
  }

  const prod = state.products.find(p => p.id === id);
  if (prod) {
    prod.availableQuantity = qty;
  }
  closeUpdateStockModal();
  renderProducts();
  populateOrderProductSelect();
  showToast(`Stock updated for product #${id}`, 'success');
}

// ===================================================================
// 5. ORDERS & KAFKA SAGA WORKFLOW
// ===================================================================
async function fetchOrders() {
  if (state.isLiveBackend) {
    try {
      const res = await fetch(`${CONFIG.GATEWAY_URL}${CONFIG.ORDERS_ENDPOINT}?page=0&size=50&sort=id,desc`, {
        headers: getAuthHeaders()
      });
      if (res.ok) {
        const data = await res.json();
        state.orders = data.content || data;
      }
    } catch (e) {
      console.warn('Using fallback orders');
    }
  }
  renderOrders();
  updateStats();
}

function renderOrders() {
  const tbody = document.getElementById('orders-table-body');
  tbody.innerHTML = '';

  if (state.orders.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-slate-500 italic">No orders created yet. Submit a new order above!</td></tr>`;
    return;
  }

  state.orders.forEach(o => {
    let statusClass = 'bg-amber-500/10 text-amber-400 border border-amber-500/20';
    if (o.status === 'CONFIRMED') statusClass = 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20';
    if (o.status === 'OUT_OF_STOCK') statusClass = 'bg-rose-500/10 text-rose-400 border border-rose-500/20';
    if (o.status === 'CANCELLED') statusClass = 'bg-slate-500/10 text-slate-400 border border-slate-500/20';

    const tr = document.createElement('tr');
    tr.className = 'hover:bg-slate-900/40 transition';
    tr.innerHTML = `
      <td class="p-3 font-mono font-bold text-slate-200">#${o.id || o.orderId}</td>
      <td class="p-3 text-slate-400">${o.customerId}</td>
      <td class="p-3 text-slate-400 font-mono">Product #${o.productId}</td>
      <td class="p-3 font-bold text-slate-200">${o.quantity}</td>
      <td class="p-3"><span class="px-2 py-0.5 rounded text-[10px] font-bold ${statusClass}">${o.status}</span></td>
      <td class="p-3 text-slate-500 text-[11px]">${o.createdAt ? new Date(o.createdAt).toLocaleTimeString() : 'Just now'}</td>
      <td class="p-3 text-right">
        ${o.status === 'CONFIRMED' || o.status === 'PENDING' ? `
          <button onclick="cancelOrder(${o.id || o.orderId})" class="text-rose-400 hover:text-rose-300 text-[11px] font-medium">Cancel</button>
        ` : `<span class="text-slate-600 text-[11px]">Finalized</span>`}
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function handleCreateOrder(e) {
  e.preventDefault();
  const customerId = parseInt(document.getElementById('order-customer-select').value, 10);
  const productId = parseInt(document.getElementById('order-product-select').value, 10);
  const quantity = parseInt(document.getElementById('order-quantity').value, 10);

  const payload = { customerId, productId, quantity };

  if (state.isLiveBackend) {
    try {
      const res = await fetch(`${CONFIG.GATEWAY_URL}${CONFIG.ORDERS_ENDPOINT}`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        const orderData = await res.json();
        showToast(`Order #${orderData.orderId} created (${orderData.status}): ${orderData.message}`, 'success');
        // Refresh orders from backend after a short delay to allow Saga to complete
        setTimeout(() => fetchOrders(), 2000);
        return;
      } else {
        const err = await res.json().catch(() => ({}));
        showToast(`Order failed: ${err.message || res.statusText}`, 'error');
        return;
      }
    } catch (err) {
      showToast(`Network error creating order: ${err.message}`, 'error');
      return;
    }
  }

  // Fallback: Demo mode Saga simulation
  const orderId = 1000 + state.orders.length + 1;
  runSagaPipeline(orderId, customerId, productId, quantity);
}

async function cancelOrder(id) {
  if (state.isLiveBackend) {
    try {
      await fetch(`${CONFIG.GATEWAY_URL}${CONFIG.ORDERS_ENDPOINT}/${id}/cancel`, {
        method: 'PUT',
        headers: getAuthHeaders()
      });
    } catch (e) {
      console.error(e);
    }
  }

  const o = state.orders.find(item => (item.id || item.orderId) === id);
  if (o) {
    o.status = 'CANCELLED';
  }
  renderOrders();
  showToast(`Order #${id} marked as CANCELLED`, 'info');
}

// ===================================================================
// 6. SAGA PIPELINE SIMULATION & REAL-TIME TRACE
// ===================================================================
async function runSagaPipeline(orderId, customerId, productId, quantity) {
  const newOrder = {
    id: orderId,
    orderId: orderId,
    customerId: customerId,
    productId: productId,
    quantity: quantity,
    status: 'PENDING',
    createdAt: new Date().toISOString()
  };

  state.orders.unshift(newOrder);
  renderOrders();
  switchTab('dashboard');

  logEventPipeline(`▶ [1/4] Client -> API Gateway :8080 -> POST /api/orders (JWT Validated)`);
  highlightNode('node-gateway');

  await sleep(700);
  logEventPipeline(`💾 [2/4] Order Service :8081 saved Order #${orderId} as [PENDING] in order_db`);
  logEventPipeline(`📨 [2/4] Order Service published event to Kafka topic: [order-placed]`);
  highlightNode('node-order');
  highlightNode('node-kafka');
  state.stats.eventsCount++;

  await sleep(900);
  logEventPipeline(`⚡ [3/4] Inventory Service :8082 consumed [order-placed] event for Product #${productId}`);
  highlightNode('node-inventory');

  // Check product stock
  const product = state.products.find(p => p.id === productId);
  const isAvailable = product && product.availableQuantity >= quantity;

  await sleep(800);
  if (isAvailable) {
    product.availableQuantity -= quantity;
    product.reservedQuantity = (product.reservedQuantity || 0) + quantity;
    newOrder.status = 'CONFIRMED';
    state.stats.confirmedCount++;
    state.stats.eventsCount++;

    logEventPipeline(`✅ [3/4] Stock available! Deducted ${quantity} units. Published [inventory-updated] (CONFIRMED)`);
    highlightNode('node-kafka');

    await sleep(700);
    logEventPipeline(`🎉 [4/4] Order Service finalized Order #${orderId} status -> [CONFIRMED]`);
    logEventPipeline(`📧 [4/4] Notification Service dispatched confirmation email/SMS to Customer #${customerId}`);
    
    addNotification({
      id: Date.now(),
      orderId: orderId,
      productId: productId,
      notificationType: 'ORDER_CONFIRMATION',
      status: 'CONFIRMED',
      message: `🎉 Order #${orderId} CONFIRMED! ${quantity}x Product #${productId} allocated. Dispatched email to Customer #${customerId}.`,
      sentAt: new Date().toISOString()
    });

    showToast(`Order #${orderId} confirmed and stock reserved!`, 'success');
  } else {
    newOrder.status = 'OUT_OF_STOCK';
    state.stats.eventsCount++;

    logEventPipeline(`⚠️ [3/4] Insufficient Stock! Available: ${product ? product.availableQuantity : 0}. Published [order-out-of-stock]`);
    highlightNode('node-kafka');

    await sleep(700);
    logEventPipeline(`❌ [4/4] Order Service finalized Order #${orderId} status -> [OUT_OF_STOCK]`);
    logEventPipeline(`📱 [4/4] Notification Service dispatched out-of-stock notice to Customer #${customerId}`);

    addNotification({
      id: Date.now(),
      orderId: orderId,
      productId: productId,
      notificationType: 'ORDER_OUT_OF_STOCK',
      status: 'OUT_OF_STOCK',
      message: `⚠️ Order #${orderId} OUT_OF_STOCK! Product #${productId} has insufficient stock. Customer notified.`,
      sentAt: new Date().toISOString()
    });

    showToast(`Order #${orderId} failed: Out of stock!`, 'error');
  }

  renderOrders();
  renderProducts();
  updateStats();
}

function triggerDemoOrderFlow() {
  const sampleOrder = {
    orderId: 1000 + state.orders.length + 1,
    customerId: 101,
    productId: 55,
    quantity: 2
  };
  runSagaPipeline(sampleOrder.orderId, sampleOrder.customerId, sampleOrder.productId, sampleOrder.quantity);
}

function logEventPipeline(msg) {
  const container = document.getElementById('event-pipeline-log');
  const div = document.createElement('div');
  div.className = 'py-0.5 border-l-2 border-indigo-500 pl-2 text-slate-200 animate-fade-in';
  div.innerText = `[${new Date().toLocaleTimeString()}] ${msg}`;
  container.prepend(div);
}

function highlightNode(nodeId) {
  const node = document.getElementById(nodeId);
  if (!node) return;
  node.classList.add('ring-2', 'ring-indigo-400', 'bg-indigo-900/50');
  setTimeout(() => {
    node.classList.remove('ring-2', 'ring-indigo-400', 'bg-indigo-900/50');
  }, 1200);
}

// ===================================================================
// 7. NOTIFICATIONS
// ===================================================================
async function fetchNotifications() {
  if (state.isLiveBackend) {
    try {
      const res = await fetch(`${CONFIG.GATEWAY_URL}${CONFIG.NOTIFICATIONS_ENDPOINT}`, {
        headers: getAuthHeaders()
      });
      if (res.ok) {
        state.notifications = await res.json();
      }
    } catch (e) {
      console.warn('Using local notifications');
    }
  }
  renderNotifications();
}

function addNotification(n) {
  state.notifications.unshift(n);
  renderNotifications();
}

function renderNotifications() {
  const feed = document.getElementById('notifications-feed');
  feed.innerHTML = '';

  document.getElementById('notif-badge').innerText = state.notifications.length;

  if (state.notifications.length === 0) {
    feed.innerHTML = `<div class="glass-panel p-6 rounded-xl text-center text-slate-500 italic">No notifications dispatched yet. Process an order to see live Kafka alerts!</div>`;
    return;
  }

  state.notifications.forEach(n => {
    const isConfirmed = n.status === 'CONFIRMED';
    const card = document.createElement('div');
    card.className = `glass-panel p-4 rounded-xl border-l-4 ${isConfirmed ? 'border-l-emerald-500' : 'border-l-rose-500'}`;
    card.innerHTML = `
      <div class="flex items-center justify-between mb-1">
        <div class="flex items-center space-x-2">
          <span class="px-2 py-0.5 rounded text-[10px] font-bold ${isConfirmed ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'}">
            ${n.notificationType}
          </span>
          <span class="text-xs font-mono text-slate-400">Order #${n.orderId}</span>
        </div>
        <span class="text-[11px] text-slate-500">${new Date(n.sentAt).toLocaleTimeString()}</span>
      </div>
      <p class="text-xs text-slate-200 mt-2">${n.message}</p>
    `;
    feed.appendChild(card);
  });
}

// ===================================================================
// 8. UTILITIES & TOASTS
// ===================================================================
function updateStats() {
  state.stats.productsCount = state.products.length;
  state.stats.ordersCount = state.orders.length;
  document.getElementById('stat-products-count').innerText = state.stats.productsCount;
  document.getElementById('stat-orders-count').innerText = state.stats.ordersCount;
  document.getElementById('stat-confirmed-count').innerText = state.stats.confirmedCount;
  document.getElementById('stat-events-count').innerText = state.stats.eventsCount;
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');

  let bgClass = 'bg-slate-900 border-slate-700 text-slate-200';
  if (type === 'success') bgClass = 'bg-emerald-950 border-emerald-700 text-emerald-200';
  if (type === 'error') bgClass = 'bg-rose-950 border-rose-700 text-rose-200';
  if (type === 'info') bgClass = 'bg-indigo-950 border-indigo-700 text-indigo-200';

  toast.className = `p-3 rounded-xl border shadow-xl text-xs flex items-center justify-between space-x-2 transition-all transform duration-300 opacity-100 ${bgClass}`;
  toast.innerHTML = `
    <span>${message}</span>
    <button onclick="this.parentElement.remove()" class="text-slate-400 hover:text-white">&times;</button>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('opacity-0', 'translate-x-full');
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
