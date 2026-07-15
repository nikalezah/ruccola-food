-- Test users (UTF-8)
-- Ivan and Petr; applied by M001 after init_inserts when loadSeedData=true

-- users
INSERT INTO public.users (id, email, password, first_name, last_name, role, created_at, updated_at)
VALUES (2, 'ivan@ruccola.test', '123qwe', 'Ivan', 'Ivanov', 'CUSTOMER', '2026-02-16 16:36:34.561213',
        '2026-02-16 16:36:34.561213'),
       (3, 'petr@ruccola.test', '123qwe', 'Petr', 'Petrov', 'CUSTOMER', '2026-02-25 10:17:36.966723',
        '2026-02-25 10:17:36.966723');

-- customers
INSERT INTO public.customers (id, address, needs_cutlery, weekend_delivery, morning_delivery)
VALUES (3, 'Seiffullina 469/1', false, false, false),
       (2, 'Akhtanova 40', false, true, false);

-- customer_plans
INSERT INTO public.customer_plans (id, customer_id, plan_id, calories, price_per_day, days, chosen_date)
VALUES (1, 2, 24, 2600, 5000, 30, '2026-04-17');

-- Sequences
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('customer_plans_id_seq', (SELECT MAX(id) FROM customer_plans));
