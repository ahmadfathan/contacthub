<script type="text/javascript">
$(document).ready(function() {
    $('.select2bs4').select2({
        theme: 'bootstrap4'
    });
});
$("#btn_show_password").click(function(){
    var txt_password = $("#password");
    if (txt_password.attr('type')=='password'){
        txt_password.prop('type','text');
        $(this).html('<i class="fa fa-eye-slash"></i>');
    }else{
        $(this).html('<i class="fa fa-eye"></i>');
        txt_password.prop('type','password');
    }
});
$("#btn_generate_password").click(function(){
    var txt_password = $("#password");
    txt_password.val(makeid(10));
});
function makeid(length) {
   var result           = '';
   var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
   var charactersLength = characters.length;
   for ( var i = 0; i < length; i++ ) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
   }
   return result;
}

</script>