var connector = remote.connect("alfresco");
var result = connector.get("/hyland/counters");

if (result.status.code == 200) {
    var data = JSON.parse(result);
    model.counters = data.counters;
    model.error = null;
} else {
    model.counters = [];
    model.error = "Failed to load counters (HTTP " + result.status.code + ")";
}
