# tms-statistikk

_samler data for bruk i statistikk._ 
Datakilder: kafkatopics og direkte kall fra FE-applikasjoner

## api

```yaml
openapi: 3.0.0
info:
  version: 1.0.0
  title: StatistikkApi
  description: Samle og henten statistikk
paths:
  /innlogging:
    post:
      description: samle innloggingsdata
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - ident
              properties:
                ident:
                  type: string
      responses:
        '201':
          description: Successful response
  /hent:
    get: 
      description: HTML side for å laste ned statistikk i CSV format
      produces: 
        - text/html
      responses: 
        '202':
          description: OK
```

Tull og tøys


