-- Reset administrator credentials for school trial.
-- Login: admin / admin123
-- The password hash is MD5 of "admin123" using the project's MD5Util (BigInteger.toString(16)).
-- V1.20 disabled this account; this migration re-enables it with the new credentials.

UPDATE `user`
SET `telephone` = 'admin',
    `password` = '192023a7bbd73250516f069df18b500',
    `status` = 1
WHERE `user_id` = 13900000000;
