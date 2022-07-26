<script type="text/javascript">
var table;
$(document).ready(function() {
    loadtable();
});
function loadtable() {
    //datatables
    table = $('#datatables').DataTable({
            'paging': true,
            'pageLength' : 20,
            'lengthChange': true,
            'searching': true,
            'ordering': true,
            'info': true,
            'autoWidth': true,
            'columns': [{
                'width': '50px'
            },
            null,
            null
        ],
        'bDestroy': true,
        'processing': true, //Feature control the processing indicator.\
        'serverSide': true, //Feature control DataTables' server-side processing mode.\
        'order': [], //Initial no order.

        // Load data for the table's content from an Ajax source
        'ajax': {
            'url': "<?= site_url('settings/ajax_list') ?>",
            'type': "POST"
        },

        //Set column definition initialisation properties.
        'columnDefs': [{
            'targets': [0], //first column / numbering column
            'orderable': false, //set not orderable
        }, ],
    });
}
function b64_to_utf8( str ) {
    return decodeURIComponent(escape(window.atob( str )));
}
</script>
