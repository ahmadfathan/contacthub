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
            null,
            null,
            null,
            null,
            null
        ],
        'bDestroy': true,
        'processing': true, //Feature control the processing indicator.\
        'serverSide': true, //Feature control DataTables' server-side processing mode.\
        'order': [], //Initial no order.

        // Load data for the table's content from an Ajax source
        'ajax': {
            'url': "<?= site_url('feed/ajax_list') ?>",
            'type': "POST"
        },

        //Set column definition initialisation properties.
        'columnDefs': [{
            'targets': [0], //first column / numbering column
            'orderable': false, //set not orderable
        }, ],
    });
}
function modalReject(action){
    $('#modal-review').modal('hide');
    $('#form-reject').attr('action',action);
    $('#modal-reject').modal();

}
function modalReview(param){
    param = decodeURIComponent(param);
    param = JSON.parse(param);
    var title = param.title;
    var image = param.image;
    var action_reject = param.action_reject;
    var action_approve = param.action_approve;
    var description = param.description;

    $('#feed-title').html(title);
    $('#feed-description').html(description);
    $('#feed-image').html('<img src="' + image + '" width="400px" height="230px" />');
    $('#btnApprove').attr('href', action_approve);
    $('#btnReject').attr('onclick', 'modalReject("'+action_reject+'");');
    $('#modal-review').modal();
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
