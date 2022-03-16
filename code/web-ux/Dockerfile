# STAGE 1 : build
FROM node:16 as builder

WORKDIR /usr/src/app

COPY . ./

RUN npm ci

RUN npm run build

# STAGE 2 : serve

FROM nginx:1.20.2

ENV PORT 7000
EXPOSE ${PORT}

COPY --from=builder /usr/src/app/dist /usr/share/nginx/html
COPY config/default.conf.template /etc/nginx/templates/default.conf.template
