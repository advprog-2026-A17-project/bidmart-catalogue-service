UPDATE listings
SET status = 'ACTIVE'
WHERE status = 'AUCTION_CREATED';

UPDATE listings
SET status = 'WON'
WHERE status = 'SOLD';
