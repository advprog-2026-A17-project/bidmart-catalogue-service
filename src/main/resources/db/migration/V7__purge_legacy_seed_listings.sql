DELETE FROM listings
WHERE seller_id IN ('seller-001', 'seller-002', 'seller-003')
   OR title IN ('Laptop Gaming ROG Strix', 'Sepeda Lipat Brompton', 'Kamera Sony A7 III')
   OR image_url IN (
       'https://dummyimage.com/400x400/000/fff&text=ROG+Strix',
       'https://dummyimage.com/400x400/000/fff&text=Brompton',
       'https://dummyimage.com/400x400/000/fff&text=Sony+A7III'
   );
