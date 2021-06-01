# STAGE 1 : build
FROM node:12 as builder

WORKDIR /usr/src/app

COPY . ./

RUN npm ci

RUN npm run build

# STAGE 2 : serve

FROM nginx:1.19

ENV API_URL localhost

COPY --from=builder /usr/src/app/dist /usr/share/nginx/html
COPY config/default.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80
