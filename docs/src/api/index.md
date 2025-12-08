<link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui.css" />
<style>
    body { margin: 0; padding: 0; }
    .md-content { max-width: none !important; padding: 0 !important; }
    .md-sidebar { display: none !important; } /* Hide sidebar for full width */
    /* Fix for dark mode text visibility in Swagger UI */
    [data-md-color-scheme="slate"] .swagger-ui .info .title,
    [data-md-color-scheme="slate"] .swagger-ui .info p,
    [data-md-color-scheme="slate"] .swagger-ui .info table,
    [data-md-color-scheme="slate"] .swagger-ui .scheme-container {
        color: var(--md-default-fg-color);
    }
</style>

<div id="swagger-ui"></div>

<script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-bundle.js" charset="UTF-8"> </script>
<script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-standalone-preset.js" charset="UTF-8"> </script>
<script>
window.onload = function() {
  const ui = SwaggerUIBundle({
    url: "openapi.yaml",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "BaseLayout"
  });
  window.ui = ui;
};
</script>