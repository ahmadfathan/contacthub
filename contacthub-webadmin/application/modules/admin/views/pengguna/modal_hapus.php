<div class="modal fade" id="modal-hapus">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title"><?= lang('label_hapus') ?></h4>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body form-horizontal">
                <div class="form-group row">
                    <label for="itemId" class="col-md-3"><?= lang('label_id') ?></label>
                    <span class="col-md-9" id="id"></span>
                </div>
                <div class="form-group row">
                    <label for="itemDescription" class="col-md-3"><?= lang('label_username') ?></label>
                    <span class="col-md-9" id="description"></span>
                </div>
            </div>
            <div class="modal-footer justify-content-between">
                <button type="button" class="btn btn-default" data-dismiss="modal"><?= lang('label_cancel') ?></button>
                <a id="btnHapus" class="btn btn-primary"><?= lang('label_delete') ?></a>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->
