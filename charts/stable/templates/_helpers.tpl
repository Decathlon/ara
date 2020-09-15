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
{{ printf "%s-%s" .Release.Name "configmap"  }}
{{- end -}}

{{/*
Secret name for database.
*/}}
{{- define "ara.database.secret.name" -}}
{{- if .Values.database.existingSecret.enabled -}}
{{ .Values.database.existingSecret.secretName }}
{{- else -}}
{{ printf "%s-%s" .Release.Name "db"  }}
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
Define access url
*/}}
{{- define "ara.db.accessUrl" -}}
{{- if .Values.database.embedded -}}
mem:{{ .Values.database.name }}
{{- else if not .Values.database.host -}}
{{/* provided database missioned by the chart */}}
{{ printf "%s-db.%s.svc.cluster.local:3306" .Chart.Name .Release.Namespace }}/{{ .Values.database.name }}
{{- else -}}
{{/* provided database missioned by the user */}}
{{ .Values.database.host }}/{{ .Values.database.name }}
{{- end -}}
{{- end -}}
