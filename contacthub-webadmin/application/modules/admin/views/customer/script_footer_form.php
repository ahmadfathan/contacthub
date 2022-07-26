<script type="text/javascript">
$(document).ready(function() {
    $('.select2bs4').select2({
        theme: 'bootstrap4'
    });
    $(document.body).on("change","#SaveContactFriendBy",function(){
        update_status('Friend',$(this).val());
    });
    $(document.body).on("change","#SaveMyContactBy",function(){
        update_status('Me',$(this).val());
    });
});
function update_status(id,value){
    if (id == 'Friend'){
        $('#DivSaveContactFriendInterest').hide();
        $('#DivSaveContactFriendReligion').hide();
        $('#DivSaveContactFriendGender').hide();
        $('#DivSaveContactFriendCity').hide();
        if (value == 'interest'){
            $('#DivSaveContactFriendInterest').show();
        }else if(value == 'Religion'){
            $('#DivSaveContactFriendReligion').show();
        }else if(value == 'Gender'){
            $('#DivSaveContactFriendGender').show();
        }else if(value == 'CityId'){
            $('#DivSaveContactFriendCity').show();
        }
    }else if(id == 'Me'){
        $('#DivSaveMyContactInterest').hide();
        $('#DivSaveMyContactReligion').hide();
        $('#DivSaveMyContactGender').hide();
        $('#DivSaveMyContactCity').hide();
        if (value == 'interest'){
            $('#DivSaveMyContactInterest').show();
        }else if(value == 'Religion'){
            $('#DivSaveMyContactReligion').show();
        }else if(value == 'Gender'){
            $('#DivSaveMyContactGender').show();
        }else if(value == 'CityId'){
            $('#DivSaveMyContactCity').show();
        }
    }
}
update_status('Friend',$('#SaveContactFriendBy').val());
update_status('Me',$('#SaveMyContactBy').val());
</script>