var init = function()
{
    var socket = new WebSocket("ws://localhost:9090");
    var lastStatus;
    var selected;

    var sendJson = function(json)
    {
        socket.send(JSON.stringify(json));
    };

    var getById = function(id)
    {
        return document.getElementById(id);
    };

    var uploadFile = function(file)
    {
        var reader = new FileReader();
        reader.onload = function(e)
                {
                    sendJson({
                        type: "file-upload",
                        contents: e.target.result,
                        name: file.name
                    });
                };
        reader.readAsText(file);
    };

    //Takes a CSS id, and a function. The function takes an object and the
    //object's index in a collection, and returns a textual representation of an
    //HTML element. Returns a function that takes a collection of objects and
    //generates child elements with the given function, and appends them to the
    //DOM element with the given id, after first emptying it.
    var updateElement = function(id, childCreator)
    {
        return function(objs)
        {
            var element = $(id);
            element.empty();
            var addChild = function(obj, i)
            {
                element.append($.parseHTML(childCreator(obj, i))[0]);
            };
            R.mapObjIndexed(addChild, objs);
        };
    };

    var updateList = updateElement("#list", function(obj, i)
    {
        return '<option value="' + i + '">' + obj.name + '</option>'
    });

    var updateTable = function(name, objs)
    {
        $("#filename").text(name);
        updateElement("#table", function(v, k)
        {
            return "<tr><td>" + k + "</td><td>" + v + "</td></tr>";
        })(objs);
    };

    var updateTableAndTitle = function(lastStatus, selected)
    {
        var args = ["No file selected", {}];
        if (lastStatus.length > selected)
        {
            args = [lastStatus[selected].name, lastStatus[selected].counts];
        }
        R.apply(updateTable, args);
    };

    //Register event handlers
    //=======================
    socket.onmessage = function(event)
    {
        lastStatus = JSON.parse(event.data);
        var sorted = R.sortBy(R.prop("name"), lastStatus);
        updateList(sorted);
        updateTableAndTitle(lastStatus, selected);
    };

    socket.onopen = function() {
        sendJson({type: "connect"});
    };

    getById("upload-button").onclick = function()
    {
        uploadFile(getById("file-browser").files[0]);
    };

    getById("list").onchange = function(event)
    {
        selected = event.target.selectedIndex;
        updateTableAndTitle(lastStatus, selected);
    };

    getById("remove-button").onclick = function()
    {
        sendJson({type: "remove-file", name: lastStatus[selected].name});
    };
};

window.onload = init;
