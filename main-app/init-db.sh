#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    DROP DATABASE IF EXISTS intershop;
    CREATE DATABASE intershop WITH ENCODING 'UTF8' TEMPLATE template0;
EOSQL
