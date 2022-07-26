<div class="modal fade" id="myModal" style="z-index:90000">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">File Manager</h4>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body form-horizontal">
                <iframe id="iframe_file_manager" width="100%" height="400px" src="<?= base_url('filemanager/dialog.php?type=2&amp;field_id=imgField\'&amp;fldr=') ?>" style="overflow: scroll; overflow-x: hidden; overflow-y: scroll; " frameborder="0"></iframe>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->
