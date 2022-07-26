<script type="text/javascript">
var table;
$(document).ready(function() {
    $('.select2bs4').select2({
        theme: 'bootstrap4'
    });
    loadtable();
    $('#CityId').on("change", function(){
        loadtable();
    });
});
function loadtable() {
    //datatables
    var city_id = $('#CityId').val();
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
            null,
            null,
            null,
            null,
            null,
        ],
        'bDestroy': true,
        'processing': true, //Feature control the processing indicator.\
        'serverSide': true, //Feature control DataTables' server-side processing mode.\
        'order': [], //Initial no order.

        // Load data for the table's content from an Ajax source
        'ajax': {
            'url': "<?= site_url('customer/ajax_list') ?>",
            'type': "POST",
            'data' : {
                "CityId" : city_id
            }
        },

        //Set column definition initialisation properties.
        'columnDefs': [{
            'targets': [0], //first column / numbering column
            'orderable': false, //set not orderable
        }, ],
    });
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
