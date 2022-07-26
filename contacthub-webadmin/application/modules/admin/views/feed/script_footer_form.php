<script type="text/javascript">
$(document).ready(function() {
    var datatable
    var selected_row = null;
    var form_group_tipe = 'add'; // add / edit
    var temp_file = null;
    setInterval(() => {
        if (temp_file != $('#temp_file').val()){
            temp_file = $('#temp_file').val();
            $('#temp_file').trigger('change');
        }
    }, 500);
    $('.select2').select2();
    $('.select2bs4').select2({
        theme: 'bootstrap4'
    });
    tinymce.init({
        selector:'textarea',
        theme: 'modern',
        height: 500,
        plugins: 'code print preview fullpage searchreplace autolink directionality visualblocks visualchars fullscreen image link media template table charmap hr pagebreak nonbreaking anchor insertdatetime advlist lists textcolor wordcount imagetools contextmenu colorpicker textpattern',
        toolbar: 'image | code | formatselect | bold italic strikethrough forecolor backcolor | link | alignleft aligncenter alignright alignjustify  | numlist bullist outdent indent  | removeformat',  // and your other buttons.
        //paste_data_images: true,
        // images_upload_handler: function (blobInfo, success, failure) {
        //     success(console.log("data:" + blobInfo.blob().type + ";base64," + blobInfo.base64()));
        // },
        // automatic_uploads: true,
        image_advtab: true,
        // images_upload_url: "<?php echo base_url("assets/tinymce_upload")?>",
        file_picker_types: 'image',
        paste_data_images:true,
        relative_urls: false,
        remove_script_host: false,
        file_picker_callback: function(cb, value, meta) {
            $('#temp_file').on('change',function(){
                cb($(this).val());
            })
            show_file_manager('temp_file');
        }
    });
});

</script>