var profile;

var openDocumentId;

function loadDocumentById(docid) {
    $('#workspace').parent().hide();
    $('#loading_document').show();
    $.ajax({ url: "/service/findingaids/" + docid,
        context: document.body,
        headers: {
            Accept : "text/html; charset=utf-8"
        },
        accepts: "text/html",
        dataType: "html",
        success: function(html) {
            openDocumentId = docid;

            $('#workspace').replaceWith("<div class=\"row\" id=\"workspace\"><h4>Working Document</h4>" + html + "</div>");

            $.ajax({ url: "/service/findingaids/profile/" + $('#profile-name').text(),
                context: document.body,
                success: function(json) {
                    profile = json;

                    markUpDiv($('.ROOT'));
                    markUpDiv($('.ROOT').find(".UNASSIGNED, .ASSIGNED, .UNASSIGNED_TABLE"));

                    $('.ROOT').addClass('well');

                    // add a download button
                    $('<button type="button" class="btn btn-default" aria-label="Left Align" id="download_ead"><span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span> Download XML</button>').insertAfter($('.ROOT'))

                    $('#download_ead').unbind();
                    $('#download_ead').click(function() {
                        var win = window.open('xml', '_blank');
                    });

                    // make it visible
                    $('#loading_document').hide();
                    $('#workspace').parent().show();
                }});

        }});
}

function markUpDiv(jquery) {
    // format the HTML and add labels and functions
    jquery.each(function() {
        var div = $(this);
        div.find(">span").each(addTextEditLinks);

        if (div.hasClass('UNASSIGNED')) {
            addToolbarForUnassigned(div);
        } else if (div.hasClass('ASSIGNED')) {
            addToolbarForAssigned(div);
        } else if (div.hasClass('UNASSIGNED_TABLE')) {
            addToolbarForTable(div);
        }
    });

    // attach assignment handler
    $('.type-assignment', jquery).change(assignType);
}


function addTextEditLinks() {
    var span = $(this);
    span.html("<a href=\"javascript:noop()\">" + span.text() + "</a>");
    span.find("a").click(function() {
        var link = $(this);
        var contents = link.text();
        var partId = link.parent().parent().attr("id");
        var fragmentId = link.parent().attr("id");
        var newValue = prompt("Edit this value", contents);
        if (newValue != null) {
            $.ajax({
                type: "PUT",
                url: partId + "/" + fragmentId,
                data: newValue,
                success: replaceDocumentElement
            });
        }
    });
}

function addDropZones(div) {
    var dropTargets = div.find('>.UNASSIGNED, >.ASSIGNED, >.UNASSIGNED_TABLE');
    dropTargets.each(function(index) {
        $(this).before('<div class="drop-zone index-' + index + '" />');
        if (index == dropTargets.size() - 1) {
            $(this).after('<div class="drop-zone index-' + (index + 1) + '" />');
        }
    });
    if (dropTargets.size() == 0) {
        div.append('<div class="drop-zone index-0" />');
    }

    div.find(".drop-zone").each(function() {
        $(this).droppable({
            greedy: true,
            hoverClass: "drop-hover",
            drop: dropComponent
        });

    });
}

function getDropZoneIndex(dropzoneDiv) {
    var index = 0;
    var classes = dropzoneDiv.attr("class").split(' ');
    for (i = 0; i < classes.length; i ++) {
        if (classes[i].indexOf("index-") == 0) {
            index = parseInt(classes[i].substring(6))
            break;
        }
    }
    return index;
}

function dropComponent(event, ui) {
    var partId = ui.draggable.attr("id");
    var newParentId = $(this).parent().attr("id");
    var index = getDropZoneIndex($(this));
    $.ajax({
        type: "MOVE",
        url: partId + "?newParent=" + newParentId + "&index=" + index,
        success: function(htmlFragment) {
            ui.draggable.remove();
            replaceDocumentElement(htmlFragment);
        }
    });
}

function addToolbarForAssigned(div) {
    var type = getPartType($(div));
    var label = getLabel(type);
    if (div.find(".ASSIGNED, .UNASSIGNED").size() == 0) {
        div.prepend('<div class="top-bar"><span class="section-label">' + label + '</span> <a href="javascript:noop()" class="reassign">(reassign)</a> <a href="javascript:noop()" class="insert">(add child)</a></div>');
        div.find('>.top-bar>.reassign').click(function() {
            var link = $(this);
            var partId = link.parent().parent().attr('id');
            var type = "UNASSIGNED";
            $.ajax({
                type: "PUT",
                url: partId + "?type=" + type,
                data: "",
                success: replaceDocumentElement
            });
            return;
        });
    } else {
        div.prepend('<div class="top-bar"><span class="section-label">' + label + '</span> <a href="javascript:noop()" class="insert">(add child)</a></div>');
    }
    div.find('>.top-bar>.insert').click(function() {
        var link = $(this);
        var index = getDropZoneIndex($(this).parent());
        var partId = link.parent().parent().attr('id');
        $.ajax({
            type: "POST",
            url: partId + "/new?index=" + index,
            data: "",
            success: replaceDocumentElement
        });
        return;

    });
    addDropZones(div);
}

function addToolbarForUnassigned(div) {
    var parentType = getPartType(div.parent());

    var toolbar = '<div class="top-bar"><select class="type-assignment"><option>Select type...</option>';
    if (parentType in profile) {
        for (i = 0; i < profile[parentType]["possibleChildren"].length; i++) {
            toolbar += '<option value="' + profile[parentType]["possibleChildren"][i] + '">' + getLabel(profile[parentType]["possibleChildren"][i]) + '</option>';
        }
    }
    toolbar += '</select> <a href="javascript:noop()" class="delete">(delete)</a></div>';
    div.prepend($(toolbar));

    div.find('>.top-bar>.delete').click(function() {
        var link = $(this);
        var partId = link.parent().parent().attr('id');
        $.ajax({
            type: "DELETE",
            url: partId,
            data: "",
            success: replaceDocumentElement
        });
        return;

    });

    div.draggable( {
        cursor: 'move',
        revert: "invalid"
    } );

}

function addToolbarForTable(div) {
    var parentType = getPartType(div.parent());
    var partId = div.attr("id");

    var table = div.find(">table");
    table.addClass("table");
    table.addClass("table-bordered");
    table.addClass("table-striped");

    var toolbar = '<div class="top-bar"><div class="instructions">The following section has been identified to be tabular data.  As such, you may download it as a spreadsheet, upload a revised spreadsheet to replace it, or make bulk assignment. <br />To make a bulk assignment: <ol><li><select class="table-type-assignment"><option>Select row type...</option>';
    if (parentType in profile) {
        for (i = 0; i < profile[parentType]["possibleChildren"].length; i++) {
            toolbar += '<option value="' + profile[parentType]["possibleChildren"][i] + '">' + getLabel(profile[parentType]["possibleChildren"][i]) + '</option>';
        }
    }
    toolbar += '</select></li><li>Select types for each column in the table below</li><li>click <button class=".btn table-assignment-button">Apply</button></li></ol></div>';
    toolbar += '</div>';
    toolbar += '<div class="spreadsheet-download"><a href="' + partId + '/table.xlsx">Download Spreadsheet</a><br /><form><input type="file" class="spreadsheet-file" /><input type="button" class="btn btn-default spreadsheet-upload-button" value="Upload Spreadsheet"></form></div>';
    div.prepend($(toolbar));

    div.find(".spreadsheet-upload-button").click(function() {
        var submit = $(this);
        var tableDiv = submit.parent().parent().parent();
        var partId = tableDiv.attr("id");
        var update_file = submit.parent().find("input").get(0).files[0];
        var reader = new FileReader();
        var xhr = new XMLHttpRequest();

        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    console.log("Status " + xhr.status );
                    replaceDocumentElement(xhr.responseText);
                } else {
                    alert("Error submitting spreadsheet!");
                }
            }
        };
        xhr.open( "PUT", partId + "/table.xlsx");
        xhr.setRequestHeader("Content-type", update_file.type || "application/octet-stream");
        reader.onload = function(e) {
            var result = e.target.result;
            var data = new Uint8Array(result.length);
            for (var i = 0; i < result.length; i++) {
                data[i] = (result.charCodeAt(i) & 0xff);
            }
            xhr.send(data.buffer);
        };
        reader.readAsBinaryString(update_file);
    });

    var header = '<thead><tr>';
    for (column = 0; column < table.find("tr:first td").length; column ++) {
        header += '<th>' + getHtmlStringForRowTypeAssignment(parentType) +  '</th>';
    }
    header += '</tr></thead>'
    table.prepend($(header));

    div.draggable( {
        cursor: 'move',
        revert: "invalid",
        helper: dragHelper
    } );

    // attach assignment handlers
    div.find(".table-type-assignment").change(selectTableAssignment);
    div.find(".table-assignment-button").click(assignTable);
    div.find(".row-type-assignment").change(validateTableForm);

    validateTableForm();
}

function validateTableForm() {
    $(".table-type-assignment").each(function() {
        var tableDiv = $(this).parents('.UNASSIGNED_TABLE');
        var applyButton = tableDiv.find(".table-assignment-button");
        var columnSelects = tableDiv.find("table thead tr th select");
        if ($(this).val() == 'Select row type...') {
            applyButton.prop('disabled', true);
            columnSelects.prop('disabled', true);
            tableDiv.find("li").removeClass("completed");
        } else {
            tableDiv.find("li:eq(0)").addClass("completed");
            tableDiv.find("li:eq(1)").addClass("completed");
            columnSelects.prop('disabled', false);
            applyButton.prop('disabled', false);
            columnSelects.each(function() {
                if ($(this).val() == "Select type for column...") {
                    applyButton.prop('disabled', true);
                    tableDiv.find("li:eq(1)").removeClass("completed");
                }
            });
        }

    });

}

function dragHelper(event) {
    return '<div id="dragging">Drop this between elements to move the table.</div>';
}

function selectTableAssignment() {
    var option = $(this);
    console.log("changed value to " + option.val());
    var table = option.parents('.UNASSIGNED_TABLE');
    table.find(".row-type-assignment").each(function() {
        var select = $(this);
        var oldval = select.val();
        select.replaceWith($(getHtmlStringForRowTypeAssignment(option.val())));
        validateTableForm();
    });
    table.find(".row-type-assignment").change(validateTableForm);

}

function getHtmlStringForRowTypeAssignment(parentType) {
    // TODO: only include text types
    var select = '<select class="row-type-assignment"><option>Select type for column...</option>';
    if (parentType in profile) {
        for (i = 0; i < profile[parentType]["possibleChildren"].length; i ++) {
            select += '<option value="' + profile[parentType]["possibleChildren"][i] + '">' + getLabel(profile[parentType]["possibleChildren"][i]) + '</option>';
        }
    }
    select += '</select>';
    return select;
}

function assignTable() {
    var button = $(this);
    var tableDiv = button.parents('.UNASSIGNED_TABLE');
    var partId = tableDiv.attr("id");
    var option = tableDiv.find(".table-type-assignment");
    if (option.val() == "Select row type...") {
        alert("You must select a type to be applied to each row in the table!");
        return;
    }
    var query = 'rowType=' + option.val();
    tableDiv.find(".row-type-assignment").each(function() {
        var select = $(this);
        if (select.val() == "Select type for column...") {
            alert("Must select type for column!");
            return;
        }
        query += "&colTypes=" + select.val();

    });
    $.ajax({
        type: "POST",
        url: partId + "/table?" + query,
        data: "",
        success: replaceDocumentElement
    });
    return;
}

function noop() {

}

function assignType() {
    var link = $(this);
    var partId = link.parent().parent().attr('id');
    var selectedType = link.val();
    console.log("SelectedType: " + selectedType);
    var canContainText = profile[selectedType]["canContainText"];
    $.ajax({
        type: canContainText ? "PUT" : "POST",
        url: partId + "?type=" + selectedType,
        success: replaceDocumentElement
    });
    return;
}

function replaceDocumentElement(htmlFragment) {
    var html = $("<div />").html(htmlFragment).children();
    console.log(html);
    var id = html.attr("id");
    console.log("id to be relpaced " + id);
    $('#' + id).replaceWith(html);
    markUpDiv($('#' + id));
    markUpDiv($('#' + id).find("div.ASSIGNED"));
    markUpDiv($('#' + id).find("div.UNASSIGNED"));
    markUpDiv($('#' + id).find("div.UNASSIGNED_TABLE"));
}

function getPartType(jquery) {
    return jquery.attr('class').split(' ')[0];
}

function getLabel(partType) {
    return profile[partType]["label"];
}
