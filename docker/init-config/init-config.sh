#!/usr/bin/env bash

if [[ $1 ]]; then
    DATA_DIR=$1
    echo "Using data directory '${DATA_DIR}' from the first argument"
else
    DATA_DIR=../data
    echo "No data directory in first argument: using '${DATA_DIR}'"
fi

CONFIG_DIR=${DATA_DIR}/server/config
CONFIG_FILE=${CONFIG_DIR}/application.properties

DEFAULT_SMTP_HOST='smtp.gmail.com'
DEFAULT_SMTP_PORT='587'

CYAN='\033[0;36m'
YELLOW='\033[0;33m'
NO_COLOR='\033[0m'

if [[ -f ${CONFIG_FILE} ]]; then
    echo "Used server configuration file: ${CONFIG_FILE}"
else
    TITLE='You need to configure how ARA will send emails (keep empty to not send emails).'
    if [[ -t 1 ]]; then
        echo
        echo -e "${CYAN}${TITLE}"
        echo -e "The settings will be written to ${CONFIG_FILE} and will be modifiable here afterward.${NO_COLOR}"
        echo
        echo "Please fill:"
        echo "* SMTP host (eg. '${DEFAULT_SMTP_HOST}'):"
        read SMTP_HOST
        echo "* SMTP port (eg. '${DEFAULT_SMTP_PORT}'):"
        read SMTP_PORT
        echo "* SMTP username (optional, could be your email address):"
        read SMTP_USERNAME
        echo "* SMTP password (optional):"
        read SMTP_PASSWORD
        echo "* SMTP auth ('true' or 'false'):"
        read SMTP_AUTH
        echo "* SMTP starts TLS ('true' or 'false'):"
        read SMTP_TLS
    else
        # Not in a TTY terminal with keyboard access: creating a (modifiable) default file
        echo
        echo -e "${YELLOW}${TITLE}"
        echo -e "Default (not-working) settings will be written to ${CONFIG_FILE} and will be modifiable here afterward.${NO_COLOR}"
        echo
        SMTP_HOST=${DEFAULT_SMTP_HOST}
        SMTP_PORT=${DEFAULT_SMTP_PORT}
        SMTP_USERNAME='<your-email-address>'
        SMTP_PASSWORD='<your-email-password>'
        SMTP_AUTH='true'
        SMTP_TLS='true'
    fi

    mkdir -p ${CONFIG_DIR}

    ( \
        echo "spring.mail.host=${SMTP_HOST}" && \
        echo "spring.mail.port=${SMTP_PORT}" && \
        echo "spring.mail.username=${SMTP_USERNAME}" && \
        echo "spring.mail.password=${SMTP_PASSWORD}" && \
        echo "spring.mail.properties.mail.smtp.auth=${SMTP_AUTH}" && \
        echo "spring.mail.properties.mail.smtp.starttls.enable=${SMTP_TLS}" \
    ) > ${CONFIG_FILE}
fi
