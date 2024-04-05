# SE PR Projekttemplate

Bitte fügen Sie diese Datei, inklusive der beiliegenden `.gitlab-ci.yml` und den beiden Verzeichnissen `frontend` und `backend` zum Wurzelverzeichnis ihres Git-Repositories hinzu.
Im folgenden befindet sich ein Template für die Stundenliste; bitte verwenden Sie es so, dass im GitLab-Projekt ihre Stundenliste als Tabelle sichtbar ist.

Vergessen Sie nicht im Projekt ihren Namen und Matrikelnummer zu ersetzen.

## Stundenliste

**Name**: Nils Schneider-Sturm\
**Matrikelnummer**: 12231214


| Datum  | Startzeit | Dauer | Story-ID | Tätigkeit                                                                       |
|--------|-----------|-------|----------|---------------------------------------------------------------------------------|
| 05.03. | 21h       | 50m   | US1      | create functionality                                                            |
| 05.03. | 22h       | 1h30  | US2      | implemented horse edit functionality                                            |
| 06.03. | 22h20     | 50m   | US4      | implemented horse detail view                                                   |
| 06.03. | 23h10     | 20m   | US3      | implemented horse delete, todo: check if in tournament                          |
| 07.03. | 13h       | 1h20m | US5      | implemented tournament search & sql create statements                           |
| 07.03. | 18h10     | 1h10  | US6      | implemented parts of tournament creation, should work now for detail view       |
|        | 22h47     | 1h    | US7      |                                                                                 |
| 05.04. | 11h33     | 3h    | US7      | implemented creation of tournament standings branches on creation of tournament |
| 05.04. | 14h33     | 1h30m | US7      | working view of tournament standings tree                                       |

**Gesamtsumme der Zeit**: 6h 

### TODO:
* horse edit error handling?
* return detail of tournament after creation properly