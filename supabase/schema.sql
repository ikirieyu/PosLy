-- ╔══════════════════════════════════════════════════════════════╗
-- ║          PosLy — Supabase PostgreSQL Schema                  ║
-- ║  Run this in your Supabase project's SQL Editor              ║
-- ╚══════════════════════════════════════════════════════════════╝

-- 1. Profiles & Hak Akses
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    role TEXT CHECK (role IN ('owner', 'worker')) DEFAULT 'worker',
    pin_code VARCHAR(6),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Pengaturan Usaha & Parameter Alokasi Dana
CREATE TABLE IF NOT EXISTS store_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_name TEXT NOT NULL DEFAULT '',
    slogan TEXT DEFAULT '',
    address TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    social_media TEXT DEFAULT '',
    logo_url TEXT,
    receipt_footer TEXT DEFAULT 'Terima kasih atas kunjungan Anda!',
    savings_percent NUMERIC(5,2) DEFAULT 30.00,
    emergency_percent NUMERIC(5,2) DEFAULT 20.00,
    restock_percent NUMERIC(5,2) DEFAULT 35.00,
    transport_percent NUMERIC(5,2) DEFAULT 15.00,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 3. Kategori Produk
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 4. Master Produk & HPP
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    sku TEXT UNIQUE,
    barcode TEXT,
    cost_price NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    selling_price NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 0,
    min_stock_alert INT DEFAULT 5,
    image_url TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 5. Transaksi / Pesanan
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number TEXT UNIQUE NOT NULL,
    cashier_id UUID REFERENCES profiles(id),
    cashier_name TEXT DEFAULT '',
    total_amount NUMERIC(12,2) NOT NULL,
    total_cost NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    discount_amount NUMERIC(12,2) DEFAULT 0.00,
    paid_amount NUMERIC(12,2) NOT NULL,
    change_amount NUMERIC(12,2) DEFAULT 0.00,
    payment_method TEXT CHECK (payment_method IN ('CASH', 'QRIS')) NOT NULL,
    status TEXT CHECK (status IN ('COMPLETED', 'VOID', 'REFUNDED')) DEFAULT 'COMPLETED',
    void_reason TEXT,
    void_approved_by UUID REFERENCES profiles(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 6. Detail Item Pesanan
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id),
    product_name TEXT NOT NULL,
    quantity INT NOT NULL,
    unit_cost NUMERIC(12,2) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    discount_per_item NUMERIC(12,2) DEFAULT 0.00,
    subtotal NUMERIC(12,2) NOT NULL,
    note TEXT DEFAULT ''
);

-- 7. Pencatatan Beban Pengeluaran (Expenses)
CREATE TABLE IF NOT EXISTS expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category TEXT CHECK (category IN ('BAHAN_BAKU', 'TRANSPORT', 'OPERASIONAL', 'DARURAT', 'LAINNYA')) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    notes TEXT DEFAULT '',
    receipt_image_url TEXT,
    created_by UUID REFERENCES profiles(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- ═══════════════════════════════════════════════════════════════
-- 8. Row Level Security (RLS) Policies
-- ═══════════════════════════════════════════════════════════════

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE store_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE expenses ENABLE ROW LEVEL SECURITY;

-- Profiles: users can only read own profile, owner can read all
CREATE POLICY "Users read own profile" ON profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Owner read all profiles" ON profiles
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'owner')
    );

CREATE POLICY "Users update own profile" ON profiles
    FOR UPDATE USING (auth.uid() = id);

-- Products: readable by all authenticated users, writable only by owner
CREATE POLICY "Authenticated read products" ON products
    FOR SELECT USING (auth.uid() IS NOT NULL);

CREATE POLICY "Owner write products" ON products
    FOR ALL USING (
        EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'owner')
    );

-- Categories: same as products
CREATE POLICY "Authenticated read categories" ON categories
    FOR SELECT USING (auth.uid() IS NOT NULL);

CREATE POLICY "Owner write categories" ON categories
    FOR ALL USING (
        EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'owner')
    );

-- Orders: cashier & owner can insert and read
CREATE POLICY "Authenticated insert orders" ON orders
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Authenticated read orders" ON orders
    FOR SELECT USING (auth.uid() IS NOT NULL);

CREATE POLICY "Owner update orders (void)" ON orders
    FOR UPDATE USING (
        EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'owner')
    );

-- Order items: follow orders policy
CREATE POLICY "Authenticated insert order_items" ON order_items
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Authenticated read order_items" ON order_items
    FOR SELECT USING (auth.uid() IS NOT NULL);

-- Expenses: owner full access, worker insert only
CREATE POLICY "Authenticated insert expenses" ON expenses
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Owner read expenses" ON expenses
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'owner')
    );

-- Store settings: owner only
CREATE POLICY "Owner read store_settings" ON store_settings
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'owner')
    );

CREATE POLICY "Owner write store_settings" ON store_settings
    FOR ALL USING (
        EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'owner')
    );

-- ═══════════════════════════════════════════════════════════════
-- 9. Storage Buckets
-- ═══════════════════════════════════════════════════════════════

-- Run separately in Supabase Storage UI or via CLI:
-- supabase storage create-bucket product-images --public
-- supabase storage create-bucket store-logos --public
-- supabase storage create-bucket expense-receipts

-- ═══════════════════════════════════════════════════════════════
-- 10. Helper function: auto-update updated_at timestamp
-- ═══════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_store_settings_updated_at BEFORE UPDATE ON store_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
