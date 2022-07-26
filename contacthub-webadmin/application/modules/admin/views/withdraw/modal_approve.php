<div class="modal fade" id="modal-approve">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title"><?= lang('label_approve') ?></h4>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body form-horizontal">
                <div class="form-group row">
                    <label class="col-sm-2 col-form-label"><?= lang('label_amount') ?> <b style="color:red">*</b></label>
                    <div class="col-sm-10">
                        <?= form_input(['name'=>'Description','type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_description'),'required' => true]) ?>
                    </div>
                </div>
            </div>
            <div class="modal-footer justify-content-between">
                <button type="button" class="btn btn-default" data-dismiss="modal"><?= lang('label_cancel') ?></button>
                <button id="btnBlock" type="button" class="btn btn-danger"><?= lang('label_approve') ?></button>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->
