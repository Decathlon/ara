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
{{- if .Values.database.secretConfig.secretName -}}
{{ .Values.database.secretConfig.secretName }}
{{- else -}}
{{ printf "%s-%s-%s" .Release.Name .Chart.Name "db"  }}
{{- end -}}
{{- end -}}

{{/*
Secret name for api/gw
*/}}
{{- define "ara.app.secret.name" -}}
{{- if .Values.app.configExistingSecret.enabled -}}
{{ .Values.app.configExistingSecret.secretName }}
{{- else -}}
{{ printf "%s-%s-%s" .Release.Name .Chart.Name "app"  }}
{{- end -}}
{{- end -}}

{{/*
Define db host url
*/}}
{{- define "ara.database.host" -}}
{{- if eq .Values.database.mode "cluster" -}}
{{ printf "%s-%s-db.%s.svc.cluster.local:5432" .Release.Name .Chart.Name .Release.Namespace }}
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
