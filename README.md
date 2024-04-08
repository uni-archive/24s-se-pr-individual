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
|        | 22h47     | 1h    | US7      | working on v                                                                    |
| 05.04. | 11h33     | 3h    | US7      | implemented creation of tournament standings branches on creation of tournament |
| 05.04. | 14h33     | 2h    | US7      | working view of tournament standings tree                                       |
| 05.04. | 21h33     | 30m   | US7      | working on v                                                                    |
| 06.04. | 13h20     | 1h    | US7      | saving and loading standings works correctly now                                |
| 06.04. | 15h40     | 1h    | US7      | working on correct match input (todo backend isLocked generation)               |
| 06.04. | 22h20     | 2.5h  | US7      | impl round reached in backend                                                   |
| 07.04. | 12h30     | 4h    | US8      | impl of generate first round                                                    |
| 07.04. | 17h30     | 45min | TS19     | working on horse validation                                                     |
| 08.04. | 19h30     | 45m   | TS19     | working on tournmanent validation                                               |
| 08.04. | 20h15     | 45m   | TS15     | checkstyle fixing for backend                                                   |

**Gesamtsumme der Zeit**: 21h 

### TODO:
* horse edit error handling?
* return detail of tournament after creation properly