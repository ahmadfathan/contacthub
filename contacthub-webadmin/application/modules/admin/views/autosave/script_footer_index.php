<script>
    function run_autosave(){
        $.ajax({
            url:"<?= base_url('autosave/run') ?>",  
            success:function(result) {
                result = JSON.parse(result);
                $('.card-body').html('<p>Status : ' + result.status + '</p><p>Total Auto Save : '+result.result.CustomerSave+'</p><p>Total Customer Credit Ready : '+result.result.CustomerReady+'</p>');
                console.log(result);
            }
        });
    }
</script>