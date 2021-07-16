# STAGE 1 : build
FROM node:12 as builder

WORKDIR /usr/src/app

COPY . ./

RUN npm ci

RUN npm run build

# STAGE 2 : serve

FROM nginx:1.19

ENV ARA_API_HOST localhost
ENV ARA_API_PORT 8080
ENV ARA_MANAGEMENT_PORT 9090

COPY --from=builder /usr/src/app/dist /usr/share/nginx/html
COPY config/default.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80
