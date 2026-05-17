-- Disable the historical development demo administrator before trial use.
-- Deployment must explicitly set an initial administrator password and enable
-- the account after database initialization.

UPDATE `user`
SET `status` = 0
WHERE `user_id` = 13900000000
  AND `password` = '1ed91049c7697d6aaf7d1959e588e735';
