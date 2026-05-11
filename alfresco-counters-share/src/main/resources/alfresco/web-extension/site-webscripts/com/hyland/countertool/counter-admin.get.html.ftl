<style>
    #hyland-counter-admin table { border-collapse: collapse; width: 100%; margin-top: 16px; }
    #hyland-counter-admin th, #hyland-counter-admin td { border: 1px solid #ccc; padding: 8px 12px; text-align: left; }
    #hyland-counter-admin th { background: #f0f0f0; }
    #hyland-counter-admin .btn { padding: 4px 10px; cursor: pointer; margin-right: 4px; border-radius: 3px; border: none; }
    #hyland-counter-admin .btn-danger  { background: #d9534f; color: #fff; }
    #hyland-counter-admin .btn-primary { background: #337ab7; color: #fff; }
    #hyland-counter-admin #createForm { margin-top: 20px; padding: 16px; border: 1px solid #ccc; background: #fafafa; max-width: 560px; }
    #hyland-counter-admin #createForm label { display: inline-block; width: 140px; }
    #hyland-counter-admin #createForm input { width: 200px; margin-bottom: 6px; padding: 4px; }
    #hyland-counter-admin .msg-error   { color: #d9534f; margin-top: 8px; }
    #hyland-counter-admin .msg-success { color: #5cb85c; margin-top: 8px; }
    #hyland-counter-admin .help-icon { display: inline-block; width: 16px; height: 16px; line-height: 16px; border-radius: 50%; background: #888; color: #fff; font-size: 11px; font-weight: bold; text-align: center; cursor: default; margin-left: 4px; vertical-align: middle; }
    #hyland-counter-admin .tooltip-box { display: none; position: absolute; z-index: 100; background: #333; color: #fff; font-size: 12px; padding: 8px 10px; border-radius: 4px; max-width: 280px; line-height: 1.5; white-space: pre-line; }
    #hyland-counter-admin .tooltip-box.visible { display: block; }
    #hyland-counter-admin .tooltip-box table { border: none; margin: 0; width: auto; }
    #hyland-counter-admin .tooltip-box th, #hyland-counter-admin .tooltip-box td { border: none; padding: 1px 6px 1px 0; background: transparent; color: #fff; font-size: 12px; }
    #hyland-counter-admin .tooltip-box th { font-weight: bold; }
</style>

<div id="hyland-counter-admin">

    <h2>Counter Admin</h2>

<#if error??>
    <p class="msg-error">${error?html}</p>
</#if>

    <div id="counter-message"></div>

    <table id="countersTable">
        <thead>
            <tr>
                <th>Name</th>
                <th>Format Template</th>
                <th>Start Value</th>
                <th>Increment</th>
                <th>Padding</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
<#list counters as c>
            <tr id="row-${c.name?html}">
                <td>${c.name?html}</td>
                <td>${(c.formatTemplate!"")?html}</td>
                <td>${c.startValue?c}</td>
                <td>${c.increment?c}</td>
                <td>${c.padding?c}</td>
                <td>
                    <button class="btn btn-danger" onclick="hylandCounterAdmin.deleteCounter('${c.name?js_string}')">Delete</button>
                </td>
            </tr>
</#list>
        </tbody>
    </table>

    <div id="createForm">
        <h3>Create Counter</h3>
        <div><label>Name:</label><input type="text" id="f-name" placeholder="e.g. invoice"/></div>
        <div style="position:relative">
            <label>Format Template:</label>
            <input type="text" id="f-template" placeholder="INV-{YYYY}{MM}-{value}"/>
            <span class="help-icon"
                  onmouseenter="hylandCounterAdmin.showTooltip(this)"
                  onmouseleave="hylandCounterAdmin.hideTooltip()">?</span>
            <div id="template-tooltip" class="tooltip-box">
                <table>
                    <tr><th>Token</th><th>Replaced with</th></tr>
                    <tr><td>{value}</td><td>Counter value, zero-padded to the padding width</td></tr>
                    <tr><td>{YYYY}</td><td>4-digit year (e.g. 2025)</td></tr>
                    <tr><td>{YY}</td><td>2-digit year (e.g. 25)</td></tr>
                    <tr><td>{MM}</td><td>2-digit month (e.g. 04)</td></tr>
                    <tr><td>{DD}</td><td>2-digit day (e.g. 07)</td></tr>
                </table>
                <div style="margin-top:6px;border-top:1px solid #555;padding-top:6px">
                    <strong>Example:</strong> template <code>INV-{YYYY}{MM}-{value}</code> with padding 6<br/>
                    → <code>INV-202504-000042</code>
                </div>
            </div>
        </div>
        <div><label>Start Value:</label><input type="number" id="f-start" value="0"/></div>
        <div><label>Increment:</label><input type="number" id="f-increment" value="1"/></div>
        <div><label>Padding:</label><input type="number" id="f-padding" value="0" min="0"/></div>
        <br/>
        <button class="btn btn-primary" onclick="hylandCounterAdmin.createCounter()">Create</button>
    </div>

</div>

<script>
(function() {
    var proxy = Alfresco.constants.PROXY_URI + "hyland/counters";

    function showMessage(msg, isError) {
        var el = document.getElementById("counter-message");
        el.className = isError ? "msg-error" : "msg-success";
        el.textContent = msg;
        setTimeout(function() { el.textContent = ""; }, 4000);
    }

    window.hylandCounterAdmin = {
        createCounter: function() {
            var payload = {
                name:           document.getElementById("f-name").value.trim(),
                formatTemplate: document.getElementById("f-template").value.trim() || "{value}",
                startValue:     parseInt(document.getElementById("f-start").value, 10) || 0,
                increment:      parseInt(document.getElementById("f-increment").value, 10) || 1,
                padding:        parseInt(document.getElementById("f-padding").value, 10) || 0
            };
            if (!payload.name) { showMessage("Name is required", true); return; }
            var xhr = new XMLHttpRequest();
            xhr.open("POST", proxy, true);
            xhr.setRequestHeader("Content-Type", "application/json");
            xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
            xhr.onload = function() {
                if (xhr.status === 201) {
                    showMessage("Counter '" + payload.name + "' created.", false);
                    setTimeout(function() { location.reload(); }, 1000);
                } else {
                    var err = JSON.parse(xhr.responseText);
                    showMessage(err.error || "Creation failed", true);
                }
            };
            xhr.send(JSON.stringify(payload));
        },

        showTooltip: function(anchor) {
            var tip = document.getElementById("template-tooltip");
            var rect = anchor.getBoundingClientRect();
            tip.style.top  = (anchor.offsetTop + anchor.offsetHeight + 4) + "px";
            tip.style.left = anchor.offsetLeft + "px";
            tip.classList.add("visible");
        },

        hideTooltip: function() {
            document.getElementById("template-tooltip").classList.remove("visible");
        },

        deleteCounter: function(name) {
            if (!confirm("Delete counter '" + name + "'?")) return;
            var xhr = new XMLHttpRequest();
            xhr.open("DELETE", proxy + "/" + encodeURIComponent(name), true);
            xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
            xhr.onload = function() {
                if (xhr.status === 200) {
                    var row = document.getElementById("row-" + name);
                    if (row) row.parentNode.removeChild(row);
                    showMessage("Counter '" + name + "' deleted.", false);
                } else {
                    var err = JSON.parse(xhr.responseText);
                    showMessage(err.error || "Deletion failed", true);
                }
            };
            xhr.send();
        }
    };
})();
</script>
