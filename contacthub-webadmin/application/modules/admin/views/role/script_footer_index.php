<script type="text/javascript">
var table;
$(document).ready(function() {
    loadtable();
});
function loadtable() {
    //datatables
    table = $('#datatables').DataTable({
            'paging': true,
            'lengthChange': true,
            'searching': true,
            'ordering': true,
            'info': true,
            'autoWidth': true,
            'columns': [{
                'width': '50px'
            },
            null,
        ],
        'bDestroy': true,
        'processing': true, //Feature control the processing indicator.\
        'serverSide': true, //Feature control DataTables' server-side processing mode.\
        'order': [], //Initial no order.

        // Load data for the table's content from an Ajax source
        'ajax': {
            'url': "<?= site_url('role/ajax_list/') ?>",
            'type': "POST"
        },

        //Set column definition initialisation properties.
        'columnDefs': [{
            'targets': [0], //first column / numbering column
            'orderable': false, //set not orderable
        }, ],
    });
}
function modal_view(param){
    param = decodeURIComponent(param);
    param = JSON.parse(param);
    $("#table-view > tbody").empty();
    for (var i = 0; i < param.length; i++) {
        var val = param[i]["value"];
        var htmlDecode;
        if((htmlDecode = $.parseHTML(val)[0]) == undefined){
            htmlDecode = val;
        }else{
            htmlDecode = $.parseHTML(val)[0]['wholeText'];
        }
        $("#table-view > tbody:last-child").append('<tr><th>' + param[i]["label"] + '</th><td>' + htmlDecode + '</td></tr>')
    }
    $('#modal-view').modal();
}
function hapusItem(param,action) {
    param = decodeURIComponent(param);
    param = JSON.parse(param);
    var id = param.id;
    var nama = param.description;

    $('#id').html(id);
    $('#description').html(nama);
    $('#btnHapus').attr('href', action);
    $('#modal-hapus').modal();
}
function b64_to_utf8( str ) {
    return decodeURIComponent(escape(window.atob( str )));
}
</script>
