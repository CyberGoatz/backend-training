# OpenSearch Event Schema

This document describes the structure of training audit events stored in OpenSearch, covering index naming, the shared base fields, and the per-event-type extra fields.

---

## Index Naming

Each training run gets its own index, named using the following pattern:

```
crczp.events.trainings.pool={poolId}.sandbox={sandboxId}.definition={definitionId}.instance={instanceId}.run={runId}
```

**Example:**
```
crczp.events.trainings.pool=4.sandbox=a33f0de1-f87f-4354-8cc7-e7a4002a729a.definition=8.instance=6.run=6
```

Indices are created automatically on first bulk write (`auto(bulk api)`) and inherit their mapping from the `template_1` index template.

---

## Inspecting the Schema

Since the cluster runs unauthenticated and is not externally exposed, you can query it directly from inside the pod or via `kubectl exec`.

**List all training indices:**
```bash
curl -s "http://localhost:9200/_cat/indices?v&pretty"
```

**Get the mapping for all training indices:**
```bash
curl -s "http://localhost:9200/crczp.events.trainings.*/_mapping?pretty"
```

**Get the index template (all indices inherit from this):**
```bash
curl -s "http://localhost:9200/_template/template_1?pretty"
```

**Sample documents from all training indices:**
```bash
curl -s "http://localhost:9200/crczp.events.trainings.*/_search?pretty&size=10"
```

**Quick overview of event types in a run:**
```bash
curl -s "http://localhost:9200/crczp.events.trainings.*/_search?pretty&size=20" | \
  python3 -c "import sys,json; [print(h['_source']['type'], '|', h['_source'].get('level_order'), '|', h['_source'].get('level_title','')) for h in json.load(sys.stdin)['hits']['hits']]"
```

---

## Base Fields

Every event stored in OpenSearch extends `AbstractAuditPOJO` and contains the following fields:

| Field | ES Type | Description |
|---|---|---|
| `type` | `text/keyword` | Fully-qualified event class name (see event types below) |
| `timestamp` | `long` | Epoch milliseconds when the event occurred |
| `sandbox_id` | `text/keyword` | UUID of the sandbox associated with the training run |
| `pool_id` | `long` | ID of the sandbox pool |
| `training_definition_id` | `long` | ID of the training definition |
| `training_instance_id` | `long` | ID of the training instance |
| `training_run_id` | `long` | ID of the training run |
| `training_time` | `long` | Milliseconds elapsed in the training run at the time of the event |
| `level` | `long` | ID of the level (generated when the definition is created) |
| `level_order` | `long` | Zero-based position of the level within the training definition |
| `user_ref_id` | `long` | ID of the participant |
| `actual_score_in_level` | `long` | Participant's current score in the level at the time of the event |
| `total_training_level_score` | `long` | Cumulative score across all training-type levels |
| `total_assessment_level_score` | `long` | Cumulative score across all assessment-type levels |

### The `syslog` Object

Every document also contains a `syslog` nested object. This is **not** part of the Java model — it is injected by the rsyslog → Logstash pipeline that ships the structured audit log into OpenSearch.

| Field | ES Type | Description |
|---|---|---|
| `syslog.@timestamp` | `date` | Timestamp from the syslog message |
| `syslog.timegenerated` | `date` | Timestamp when rsyslog received the message |
| `syslog.host` | `text/keyword` | Source host (JSON-encoded, e.g. `{"ip":"10.42.0.159"}`) |
| `syslog.fromhost-ip` | `text/keyword` | Source IP (dash-separated format, e.g. `10-42-0-181`) |
| `syslog.programname` | `text/keyword` | Always `backend-training` |
| `syslog.severity` | `text/keyword` | Syslog severity level (e.g. `info`) |
| `syslog.facility` | `text/keyword` | Syslog facility (e.g. `security`) |
| `syslog.procid` | `text/keyword` | Process ID (typically empty) |
| `syslog.type` | `text/keyword` | Always `syslog` |
| `syslog.@version` | `text/keyword` | Logstash version tag |

---

## Event Types

The `type` field identifies the event. Each event type may carry additional fields on top of the base fields.

### `TrainingRunStarted`
```
cz.cyberrange.platform.events.trainings.TrainingRunStarted
```
Fired when a participant starts a training run. No extra fields beyond the base.

---

### `TrainingRunResumed`
```
cz.cyberrange.platform.events.trainings.TrainingRunResumed
```
Fired when a participant resumes a previously paused training run. No extra fields beyond the base.

---

### `TrainingRunEnded`
```
cz.cyberrange.platform.events.trainings.TrainingRunEnded
```
Fired when a training run is completed.

| Field | ES Type | Description |
|---|---|---|
| `start_time` | `long` | Epoch ms when the run started |
| `end_time` | `long` | Epoch ms when the run ended |

---

### `LevelStarted`
```
cz.cyberrange.platform.events.trainings.LevelStarted
```
Fired when a participant enters a level.

| Field | ES Type | Description |
|---|---|---|
| `level_type` | `text/keyword` | Type of level: `INFO`, `ACCESS`, `TRAINING`, `ASSESSMENT` |
| `level_title` | `text/keyword` | Display name of the level |
| `max_score` | `long` | Maximum achievable score for this level |

---

### `LevelCompleted`
```
cz.cyberrange.platform.events.trainings.LevelCompleted
```
Fired when a participant completes a level.

| Field | ES Type | Description |
|---|---|---|
| `level_type` | `text/keyword` | Type of level: `INFO`, `ACCESS`, `TRAINING`, `ASSESSMENT` |

---

### `CorrectAnswerSubmitted`
```
cz.cyberrange.platform.events.trainings.CorrectAnswerSubmitted
cz.cyberrange.platform.events.trainings.CorrectFlagSubmitted  (legacy alias)
```
Fired when a participant submits the correct answer/flag for a training level.

| Field | ES Type | Description |
|---|---|---|
| `answer_content` | `text/keyword` | The correct answer/flag that was submitted |

---

### `WrongAnswerSubmitted`
```
cz.cyberrange.platform.events.trainings.WrongAnswerSubmitted
cz.cyberrange.platform.events.trainings.WrongFlagSubmitted  (legacy alias)
```
Fired on each incorrect answer/flag submission.

| Field | ES Type | Description |
|---|---|---|
| `answer_content` | `text/keyword` | The incorrect answer/flag that was submitted |
| `count` | `long` | Sequential number of this wrong attempt (1 = first wrong try) |

---

### `CorrectPasskeySubmitted`
```
cz.cyberrange.platform.events.trainings.CorrectPasskeySubmitted
```
Fired when a participant submits the correct passkey on an ACCESS level.

| Field | ES Type | Description |
|---|---|---|
| `passkey_content` | `text/keyword` | The passkey that was submitted |

---

### `WrongPasskeySubmitted`
```
cz.cyberrange.platform.events.trainings.WrongPasskeySubmitted
```
Fired on each incorrect passkey submission on an ACCESS level.

| Field | ES Type | Description |
|---|---|---|
| `passkey_content` | `text/keyword` | The incorrect passkey that was submitted |

---

### `HintTaken`
```
cz.cyberrange.platform.events.trainings.HintTaken
```
Fired when a participant reveals a hint.

| Field | ES Type | Description |
|---|---|---|
| `hint_id` | `long` | ID of the hint |
| `hint_title` | `text/keyword` | Display name of the hint |
| `hint_penalty_points` | `long` | Score deducted for taking this hint |

---

### `SolutionDisplayed`
```
cz.cyberrange.platform.events.trainings.SolutionDisplayed
```
Fired when a participant reveals the solution for a level.

| Field | ES Type | Description |
|---|---|---|
| `penalty_points` | `long` | Score deducted for revealing the solution |

---

### `AssessmentAnswers`
```
cz.cyberrange.platform.events.trainings.AssessmentAnswers
```
Fired when a participant submits answers on an ASSESSMENT level.

| Field | ES Type | Description |
|---|---|---|
| `answers` | dynamic object | JSON payload containing the submitted assessment answers. Structure varies by assessment definition. |

---

## Notes

- The `CorrectFlagSubmitted` and `WrongFlagSubmitted` type names are legacy aliases — the Java model deserializes both to `CorrectAnswerSubmitted` / `WrongAnswerSubmitted` respectively. New events will always use the non-Flag names.
- `training_time` uses milliseconds and reflects elapsed time within the run, not wall-clock time.
- The `level` field is the database ID of the level entity, while `level_order` is its zero-based position in the definition. Use `level_order` for ordering, not `level`.
- ES types are inferred dynamically (no explicit mapping template for most fields), so `long` in Java lands as `long` in ES and `int` also lands as `long` due to dynamic mapping rules.
