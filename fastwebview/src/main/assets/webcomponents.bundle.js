/**
 * Mock WebComponents bundle for Zero-Latency FastWebView Playground
 * Contains: <checkout-widget> and <react-mock-component>
 */

class CheckoutWidget extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.state = {
            title: 'Shopping Cart Checkout',
            price: 149.99,
            quantity: 1,
            items: ['Item A', 'Item B']
        };
    }

    connectedCallback() {
        this.render();
    }

    setProps(props) {
        this.state = { ...this.state, ...props };
        this.render();
    }

    onReset() {
        this.state = { title: 'Checkout', price: 0, quantity: 0, items: [] };
    }

    render() {
        const total = (this.state.price * this.state.quantity).toFixed(2);
        this.shadowRoot.innerHTML = `
            <style>
                :host {
                    display: block;
                    font-family: system-ui, -apple-system, sans-serif;
                    background: #ffffff;
                    border: 1px solid #e0e0e0;
                    border-radius: 16px;
                    padding: 20px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
                }
                .header {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    border-bottom: 2px solid #f0f0f0;
                    padding-bottom: 12px;
                    margin-bottom: 16px;
                }
                .title {
                    font-size: 18px;
                    font-weight: 700;
                    color: #1a1a1a;
                    margin: 0;
                }
                .badge {
                    background: #e8f5e9;
                    color: #2e7d32;
                    padding: 4px 10px;
                    border-radius: 20px;
                    font-size: 12px;
                    font-weight: 600;
                }
                .item-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin: 12px 0;
                    font-size: 14px;
                }
                .btn-group {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }
                button {
                    background: #6200ee;
                    color: white;
                    border: none;
                    border-radius: 8px;
                    padding: 8px 16px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: background 0.2s;
                }
                button:hover {
                    background: #3700b3;
                }
                .btn-outline {
                    background: transparent;
                    color: #6200ee;
                    border: 1px solid #6200ee;
                }
                .total-row {
                    display: flex;
                    justify-content: space-between;
                    font-size: 18px;
                    font-weight: 800;
                    margin-top: 20px;
                    padding-top: 12px;
                    border-top: 2px dashed #e0e0e0;
                    color: #111;
                }
            </style>
            <div class="header">
                <h3 class="title">${this.state.title}</h3>
                <span class="badge">Mojo IPC FastWeb</span>
            </div>
            <div class="item-row">
                <span>Unit Price: $${this.state.price}</span>
                <div class="btn-group">
                    <button class="btn-outline" id="btn-dec">-</button>
                    <span>Qty: ${this.state.quantity}</span>
                    <button class="btn-outline" id="btn-inc">+</button>
                </div>
            </div>
            <div class="total-row">
                <span>Total:</span>
                <span>$${total}</span>
            </div>
            <div style="margin-top: 16px; display: flex; gap: 10px;">
                <button id="btn-pay" style="flex: 1; padding: 12px;">Complete Payment</button>
                <button id="btn-binary" class="btn-outline">Send Binary</button>
            </div>
        `;

        this.shadowRoot.getElementById('btn-dec')?.addEventListener('click', () => {
            if (this.state.quantity > 1) {
                this.state.quantity--;
                this.render();
                if (window.emitToNative) {
                    window.emitToNative({ type: "QUANTITY_CHANGED", quantity: this.state.quantity });
                }
            }
        });

        this.shadowRoot.getElementById('btn-inc')?.addEventListener('click', () => {
            this.state.quantity++;
            this.render();
            if (window.emitToNative) {
                window.emitToNative({ type: "QUANTITY_CHANGED", quantity: this.state.quantity });
            }
        });

        this.shadowRoot.getElementById('btn-pay')?.addEventListener('click', () => {
            if (window.emitToNative) {
                window.emitToNative({
                    type: "CHECKOUT_COMPLETED",
                    amount: (this.state.price * this.state.quantity).toFixed(2),
                    quantity: this.state.quantity,
                    timestamp: Date.now()
                });
            }
        });

        this.shadowRoot.getElementById('btn-binary')?.addEventListener('click', () => {
            if (window.emitToNative) {
                const buffer = new ArrayBuffer(16);
                const view = new Uint8Array(buffer);
                for (let i = 0; i < 16; i++) view[i] = i * 16 + 1;
                window.emitToNative(buffer);
            }
        });
    }
}

class ReactMockComponent extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.state = {
            componentName: 'React Mock Component',
            count: 0,
            status: 'Active'
        };
    }

    connectedCallback() {
        this.render();
    }

    setProps(props) {
        this.state = { ...this.state, ...props };
        this.render();
    }

    onReset() {
        this.state = { componentName: 'Reset Component', count: 0, status: 'Idle' };
    }

    render() {
        this.shadowRoot.innerHTML = `
            <style>
                :host {
                    display: block;
                    font-family: system-ui, -apple-system, sans-serif;
                    background: #1e1e2e;
                    color: #cdd6f4;
                    border-radius: 16px;
                    padding: 20px;
                    box-shadow: 0 4px 16px rgba(0,0,0,0.2);
                }
                .badge {
                    background: #89b4fa;
                    color: #11111b;
                    padding: 4px 8px;
                    border-radius: 6px;
                    font-size: 12px;
                    font-weight: 700;
                }
                .card {
                    background: #313244;
                    border-radius: 12px;
                    padding: 16px;
                    margin-top: 12px;
                }
                button {
                    background: #a6e3a1;
                    color: #11111b;
                    border: none;
                    border-radius: 8px;
                    padding: 10px 16px;
                    font-weight: 700;
                    cursor: pointer;
                    margin-top: 10px;
                }
            </style>
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <h2>${this.state.componentName}</h2>
                <span class="badge">Stateful WebComponent</span>
            </div>
            <div class="card">
                <p>Counter State: <strong>${this.state.count}</strong></p>
                <p>Status: <strong>${this.state.status}</strong></p>
                <button id="btn-count">Increment Counter</button>
            </div>
        `;

        this.shadowRoot.getElementById('btn-count')?.addEventListener('click', () => {
            this.state.count++;
            this.render();
            if (window.emitToNative) {
                window.emitToNative({
                    type: "COUNTER_UPDATED",
                    count: this.state.count,
                    timestamp: Date.now()
                });
            }
        });
    }
}

customElements.define('checkout-widget', CheckoutWidget);
customElements.define('react-mock-component', ReactMockComponent);
