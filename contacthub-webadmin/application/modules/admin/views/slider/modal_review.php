<div class="modal fade" id="modal-review">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title"><?= lang('label_review') ?></h4>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body form-horizontal">
                <div class="form-group row">
                    <label for="itemId" class="col-md-3"><?= lang('label_title') ?></label>
                    <span class="col-md-9" id="feed-title"></span>
                </div>
                <div class="form-group row">
                    <label for="itemImage" class="col-md-3"><?= lang('label_image') ?></label>
                    <span class="col-md-9" id="feed-image"></span>
                </div>
                <div class="form-group row">
                    <label for="itemDescription" class="col-md-3"><?= lang('label_description') ?></label>
                    <span class="col-md-9" id="feed-description"></span>
                </div>
            </div>
            <div class="modal-footer justify-content-between">
                <button type="button" class="btn btn-default" data-dismiss="modal"><?= lang('label_cancel') ?></button>
                <a id="btnApprove" class="btn btn-primary"><?= lang('label_approve') ?></a>

                <div class="form-group row">
                    <label for="itemReason" class="col-md-3"><?= lang('label_reason') ?></label>
                    <input type="text" class="form-control">
                    <a id="btnReject" class="btn btn-primary"><?= lang('label_reject') ?></a>
                </div>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->
