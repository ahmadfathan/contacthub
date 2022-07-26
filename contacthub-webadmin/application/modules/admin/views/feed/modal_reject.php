<div class="modal fade" id="modal-reject">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="form-reject" action="" method="POST">
                <div class="modal-header">
                    <h4 class="modal-title"><?= lang('label_reject') ?></h4>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body form-horizontal">
                    <div class="form-group row">
                        <label class="col-md-3"><?= lang('label_reason') ?></label>
                        <input type="text" class="form-control" name="Reason">
                    </div>
                </div>
                <div class="modal-footer justify-content-between">
                    <button type="button" class="btn btn-default" data-dismiss="modal"><?= lang('label_cancel') ?></button>
                    <button type="submit" class="btn btn-danger" ><?= lang('label_reject') ?></button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->
