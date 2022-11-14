# STAGE 1 : build
FROM node:12 as builder

WORKDIR /usr/src/app

COPY . ./

RUN npm ci

RUN npm run build

# STAGE 2 : serve

FROM nginx:1.19

ENV ARA_API_HOST localhost
ENV ARA_API_PORT 8000
ENV ARA_MANAGEMENT_PORT 8001
ENV PORT 7000
EXPOSE 7000

COPY --from=builder /usr/src/app/dist /usr/share/nginx/html
COPY config/default.conf.template /etc/nginx/templates/default.conf.template
