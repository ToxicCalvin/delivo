#!/bin/sh
(
set -eu

: "${ADMIN_APP_PASSWORD:?Set ADMIN_APP_PASSWORD before the first database startup}"
admin_hash="$(printf '%s' "$ADMIN_APP_PASSWORD" | md5sum | cut -d ' ' -f 1)"
MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --protocol=socket -uroot "$MYSQL_DATABASE" <<SQL
UPDATE employee SET password = '$admin_hash' WHERE username = 'admin';
SQL
)
