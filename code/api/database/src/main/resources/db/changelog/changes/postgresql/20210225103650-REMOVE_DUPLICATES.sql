--liquibase formatted sql
--changeset omar-chahbouni-decathlon:1614245956000
DELETE FROM execution
WHERE id NOT IN (SELECT *
                 FROM (SELECT MIN(e.id)
                       FROM execution e
                       GROUP BY job_url) x);
