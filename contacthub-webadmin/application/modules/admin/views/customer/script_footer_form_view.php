<script type="text/javascript">
$(document).ready(function() {
    loadtable();
    loadtable_save();
    loadtable_share();
    loadtable_penghasilan();
});
function loadtable() {
    //datatables
    table = $('#datatables-downline').DataTable({
            'paging': true,
            'lengthChange': true,
            'searching': true,
            'ordering': true,
            'info': true,
            'autoWidth': true,
            'columns': [
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
            'url': "<?= site_url('customer/ajax_list_downline') ?>",
            'type': "POST",
            'data' : {
                "UserId" : "<?= @$data['Customer']->UserId ?>"
            }
        },

        //Set column definition initialisation properties.
        'columnDefs': [{
            'targets': [0], //first column / numbering column
            'orderable': false, //set not orderable
        }, ],
    });
}
function loadtable_save() {
    //datatables
    table = $('#datatables-save').DataTable({
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
        ],
        'bDestroy': true,
        'processing': true, //Feature control the processing indicator.\
        'serverSide': true, //Feature control DataTables' server-side processing mode.\
        'order': [], //Initial no order.

        // Load data for the table's content from an Ajax source
        'ajax': {
            'url': "<?= site_url('customer/ajax_list_save') ?>",
            'type': "POST",
            'data' : {
                "UserId" : "<?= @$data['Customer']->UserId ?>"
            }
        },

        //Set column definition initialisation properties.
        'columnDefs': [{
            'targets': [0], //first column / numbering column
            'orderable': false, //set not orderable
        }, ],
    });
}
function loadtable_share() {
    //datatables
    table = $('#datatables-share').DataTable({
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
        ],
        'bDestroy': true,
        'processing': true, //Feature control the processing indicator.\
        'serverSide': true, //Feature control DataTables' server-side processing mode.\
        'order': [], //Initial no order.

        // Load data for the table's content from an Ajax source
        'ajax': {
            'url': "<?= site_url('customer/ajax_list_share') ?>",
            'type': "POST",
            'data' : {
                "UserId" : "<?= @$data['Customer']->UserId ?>"
            }
        },

        //Set column definition initialisation properties.
        'columnDefs': [{
            'targets': [0], //first column / numbering column
            'orderable': false, //set not orderable
        }, ],
    });
}
function loadtable_penghasilan() {
    //datatables
    table = $('#datatables-penghasilan').DataTable({
            'paging': true,
            'lengthChange': true,
            'searching': true,
            'ordering': true,
            'info': true,
            'autoWidth': true,
            'columns': [
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
            'url': "<?= site_url('customer/ajax_list_penghasilan') ?>",
            'type': "POST",
            'data' : {
                "UserId" : "<?= @$data['Customer']->UserId ?>"
            }
        },

        //Set column definition initialisation properties.
        'columnDefs': [{
            'targets': [0], //first column / numbering column
            'orderable': false, //set not orderable
        }, ],
    });
}
</script>