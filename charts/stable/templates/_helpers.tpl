{{/* vim: set filetype=mustache: */}}
{{/*
Create a default qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a name.
*/}}
{{- define "ara.name" -}}
{{- if .Values.nameOverride -}}
{{- .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "ara.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create configmap name.
*/}}
{{- define "ara.configmap.name" -}}
{{ printf "%s-%s-%s" .Release.Name .Chart.Name "configmap"  }}
{{- end -}}

{{/*
Secret name for database.
*/}}
{{- define "ara.database.secret.name" -}}
{{- if .Values.database.existingSecret.enabled -}}
{{ .Values.database.existingSecret.secretName }}
{{- else -}}
{{ printf "%s-%s-%s" .Release.Name .Chart.Name "db"  }}
{{- end -}}
{{- end -}}

{{/*
Secret name for api.
*/}}
{{- define "ara.api.secret.name" -}}
{{- if .Values.api.configExistingSecret.enabled -}}
{{ .Values.api.configExistingSecret.secretName }}
{{- else -}}
{{ printf "%s-%s-%s" .Release.Name .Chart.Name "api"  }}
{{- end -}}
{{- end -}}

{{/*
Secret auth config key for api.
*/}}
{{- define "ara.api.secret.authconfigkey" -}}
{{- if .Values.api.configExistingSecret.enabled -}}
{{ .Values.api.configExistingSecret.authConfigKey }}
{{- else -}}
config-authentication.yaml
{{- end -}}
{{- end -}}

{{/*
Secret custom config key for api.
*/}}
{{- define "ara.api.secret.customconfigkey" -}}
{{- if .Values.api.configExistingSecret.enabled -}}
{{ .Values.api.configExistingSecret.customConfigKey }}
{{- else -}}
config-custom.yaml
{{- end -}}
{{- end -}}

{{/*
Secret username key for database.
*/}}
{{- define "ara.database.secret.usernamekey" -}}
{{- if .Values.database.existingSecret.enabled -}}
{{ .Values.database.existingSecret.usernameKey }}
{{- else -}}
db-username
{{- end -}}
{{- end -}}

{{/*
Secret password key for database.
*/}}
{{- define "ara.database.secret.passwordkey" -}}
{{- if .Values.database.existingSecret.enabled -}}
{{ .Values.database.existingSecret.passwordKey }}
{{- else -}}
db-password
{{- end -}}
{{- end -}}

{{/*
Secret database name key for database.
*/}}
{{- define "ara.database.secret.databasenamekey" -}}
{{- if .Values.database.existingSecret.enabled -}}
{{ .Values.database.existingSecret.databaseNameKey }}
{{- else -}}
db-name
{{- end -}}
{{- end -}}

{{/*
Define host url
*/}}
{{- define "ara.db.host" -}}
{{- if .Values.database.embedded -}}
mem
{{- else if not .Values.database.host -}}
{{ printf "%s-%s-db.%s.svc.cluster.local:3306" .Release.Name .Chart.Name .Release.Namespace }}
{{- else -}}
{{ .Values.database.host }}
{{- end -}}
{{- end -}}

{{/*
Define logging mode
*/}}
{{- define "ara.api.loggingmode" -}}
{{- join "," .Values.api.loggingMode }}
{{- end -}}
