<!-- Content Header (Page header) -->
<section class="content-header">
    <div class="container-fluid">
        <div class="row mb-2">
            <div class="col-sm-6">
                <h1><?= title() ?></h1>
            </div>
            <div class="col-sm-6">
                <ol class="breadcrumb float-sm-right">
                    <li class="breadcrumb-item"><?= anchor($action_home,lang('label_home')) ?></li>
                    <li class="breadcrumb-item active"><?= $breadcrumb_active ?></li>
                </ol>
            </div>
        </div>
    </div><!-- /.container-fluid -->
</section>

<!-- Main content -->
<section class="content">
    <div class="row">
        <div class="col-6">
            <div class="card">
                <div class="card-header">
                <h3 class="card-title"><?= lang('label_form_customer') ?></h3>
              </div>
              <?= form_open_multipart('#',array('class'=>'form-horizontal')) ?>
                <form class="form-horizontal">
                    <div class="card-body">
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_greeting') ?></label>
                            <div class="col-sm-7">
                            <?= form_input(['name'=>'Greeting','value'=>@$data['Customer']->Greeting,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_nama') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Name','value'=>@$data['Customer']->Name,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_nickname') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Nickname','value'=>@$data['Customer']->User->Nickname,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_whatsapp') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'WhatsApp','value'=>@$data['Customer']->WhatsApp,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_address') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Address','value'=>@$data['Customer']->Address,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_religion') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Religion','value'=>@$data['Customer']->Religion,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_gender') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Gender','value'=>@$data['Customer']->Gender,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_business_name') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'BusinessName','value'=>@$data['Customer']->BusinessName,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_email') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Email','value'=>@$data['Customer']->User->Email,'type'=>'email','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_date_of_birth') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'DateOfBirth','value'=>@$data['Customer']->DateOfBirth,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_business_type') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'BusinessTypeId','value'=>@$data['Customer']->BusinessTypeId,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_profession') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'ProfessionId','value'=>@$data['Customer']->ProfessionId,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_save_contact_friend_by') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'SaveContactFriendBy','value'=>@$data['Customer']->SaveContactFriendBy->Type,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <?php if(@$data['Customer']->SaveContactFriendBy->Type == 'Ketertarikan'){ ?> 
                            <div class="form-group row">
                                <label class="col-sm-5 col-form-label"><?= lang('label_save_contact_friend_interest') ?></label>
                                <div class="col-sm-7">
                                <?php  $interest_list_friend = '';
                                    if (is_array(@$data['Customer']->SaveContactFriendBy->InterestId) && count(@$data['Customer']->SaveContactFriendBy->InterestId) > 0){
                                        foreach (@$data['Customer']->SaveContactFriendBy->InterestId as $item) {
                                            $interest_list_friend .= $item.', ';
                                        }
                                    }
                                ?>
                                <?= form_input(['name'=>'SaveContactFriendInterest','value'=>$interest_list_friend,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                                </div>
                            </div>
                        <?php }else if(@$data['Customer']->SaveContactFriendBy->Type =='Jenis Kelamin'){ ?>
                            <div class="form-group row">
                                <label class="col-sm-5 col-form-label"><?= lang('label_save_contact_friend_gender') ?></label>
                                <div class="col-sm-7">
                                    <?= form_input(['name'=>'SaveContactFriendGender','value'=> @$data['Customer']->SaveContactFriendBy->OtherValue ,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                                </div>
                            </div>
                        <?php }else if(@$data['Customer']->SaveContactFriendBy->Type =='Agama'){ ?>
                            <div class="form-group row">
                                <label class="col-sm-5 col-form-label"><?= lang('label_save_contact_friend_religion') ?></label>
                                <div class="col-sm-7">
                                    <?= form_input(['name'=>'SaveContactFriendReligion','value'=> @$data['Customer']->SaveContactFriendBy->OtherValue ,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                                </div>
                            </div>
                        <?php }else if(@$data['Customer']->SaveContactFriendBy->Type =='Kota'){ ?>
                            <div class="form-group row">
                                <label class="col-sm-5 col-form-label"><?= lang('label_save_contact_friend_city') ?></label>
                                <div class="col-sm-7">
                                    <?= form_input(['name'=>'SaveContactFriendCityId','value'=> @$data['Customer']->SaveContactFriendBy->OtherValue ,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                                </div>
                            </div>
                        <?php } ?>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_save_my_contact_by') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'SaveMyContactBy','value'=> @$data['Customer']->SaveMyContactBy->Type ,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <?php if(@$data['Customer']->SaveMyContactBy->Type =='Ketertarikan'){ ?>
                            <div class="form-group row">
                                <label class="col-sm-5 col-form-label"><?= lang('label_save_my_contact_interest') ?></label>
                                <div class="col-sm-7">
                                    <?php  $interest_list_me = '';
                                        if (is_array(@$data['Customer']->SaveMyContactBy->InterestId) && count(@$data['Customer']->SaveMyContactBy->InterestId) > 0) {
                                            foreach (@$data['Customer']->SaveMyContactBy->InterestId as $item) {
                                                $interest_list_me .= $item.', ';
                                            }
                                        }
                                    ?>
                                    <?= form_input(['name'=>'SaveMyContactInterest','value'=>@$interest_list_me,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                                </div>
                            </div>
                        
                        <?php }else if(@$data['Customer']->SaveMyContactBy->Type =='Jenis Kelamin'){ ?>
                            <div class="form-group row">
                                <label class="col-sm-5 col-form-label"><?= lang('label_save_my_contact_gender') ?></label>
                                <div class="col-sm-7">
                                    <?= form_input(['name'=>'SaveMyContactGender','value'=> @$data['Customer']->SaveMyContactBy->OtherValue ,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                                </div>
                            </div>
                        <?php }else if(@$data['Customer']->SaveMyContactBy->Type =='Agama'){ ?>
                            <div class="form-group row">
                                <label class="col-sm-5 col-form-label"><?= lang('label_save_my_contact_religion') ?></label>
                                <div class="col-sm-7">
                                    <?= form_input(['name'=>'SaveMyContactReligion','value'=> @$data['Customer']->SaveMyContactBy->OtherValue ,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                                </div>
                            </div>
                        <?php }else if(@$data['Customer']->SaveMyContactBy->Type =='Kota'){ ?>
                            <div class="form-group row">
                                <label class="col-sm-5 col-form-label"><?= lang('label_save_my_contact_city') ?></label>
                                <div class="col-sm-7">
                                    <?= form_input(['name'=>'SaveMyContactCityId','value'=> @$data['Customer']->SaveMyContactBy->OtherValue ,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                                </div>
                            </div>
                        <?php } ?>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_relationship_status') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'RelationshipStatus','value'=>@$data['Customer']->RelationshipStatus,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_interest') ?></label>
                            <div class="col-sm-7">
                                <?php  $interest_list = '';
                                    if (is_array(@$data['Customer']->InterestId) && count(@$data['Customer']->InterestId) > 0){
                                        foreach (@$data['Customer']->InterestId as $item) {
                                            $interest_list .= $item.', ';
                                        }
                                    }
                                ?>
                                <?= form_input(['name'=>'InterestId','value'=>@$interest_list,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_city') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'CityName','value'=>@$data['Customer']->CityName,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_hoby') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Hoby','value'=>@$data['Customer']->Hoby,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_facebook') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Facebook','value'=>@$data['Customer']->Facebook,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_instagram') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Instagram','value'=>@$data['Customer']->Instagram,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_limit_save_contact_friend_day') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'LimitSaveContactFriendDay','value'=>@$data['Customer']->LimitSaveContactFriendDay,'type'=>'number','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_limit_save_my_contact_day') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'LimitSaveMyContactDay','value'=>@$data['Customer']->LimitSaveMyContactDay,'type'=>'number','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_marketing_code') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'MarketingCode','value'=>@$data['Customer']->MarketingCode,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_product') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Product','value'=>@$data['Customer']->Product,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_website') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Website','value'=>@$data['Customer']->Website,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_username') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Username','value'=>@$data['Customer']->User->Username,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_allow_share_profile') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'AllowedShareProfile','value'=>(@$data['Customer']->AllowedShareProfile == true ? 'Ya' : 'Tidak'),'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_is_complete') ?></label>
                            <div class="col-sm-7">

                                <?= form_input(['name'=>'IsComplete','value'=>(@$data['Customer']->IsCompleted == true ? 'Lengkap' : 'Tidak Lengkap' ),'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_register_at') ?></label>
                            <div class="col-sm-7">

                                <?= form_input(['name'=>'IsComplete','value'=>@$data['Customer']->CreatedAt,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_total_credit') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'TotalCredit','value'=>number_format(@$data['Credit']->Balance),'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                    </div>
                    <!-- /.card-body -->
                    <div class="card-footer">
                    </div>
                    <!-- /.card-footer -->
                <?= form_close() ?>
            </div>
        </div>
        <div class="col-6">
            <div class="card">
                <div class="card-header">
                <h3 class="card-title"><?= lang('label_form_upline').'&nbsp;&nbsp;'; ?><?= (@$data['Upline'] == [] ? '' : anchor($link_upline,'Lihat Detail',array('class'=>'btn-xs btn btn-primary')))  ?></h3>
              </div>
              <?= form_open_multipart('#',array('class'=>'form-horizontal')) ?>
                <form class="form-horizontal">
                    <div class="card-body">
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_greeting') ?></label>
                            <div class="col-sm-7">
                            <?= form_input(['name'=>'Greeting','value'=>@$data['Upline']->Greeting,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_nama') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Name','value'=>@$data['Upline']->Name,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_nickname') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Nickname','value'=>@$data['Upline']->User->Nickname,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_whatsapp') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'WhatsApp','value'=>@$data['Upline']->WhatsApp,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_address') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Address','value'=>@$data['Upline']->Address,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_religion') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Religion','value'=>@$data['Upline']->Religion,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_gender') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Gender','value'=>@$data['Upline']->Gender,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_business_name') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'BusinessName','value'=>@$data['Upline']->BusinessName,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_email') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Email','value'=>@$data['Upline']->User->Email,'type'=>'email','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_date_of_birth') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'DateOfBirth','value'=>@$data['Upline']->DateOfBirth,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_business_type') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'BusinessTypeId','value'=>@$data['Upline']->BusinessTypeId,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_profession') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'ProfessionId','value'=>@$data['Upline']->ProfessionId,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_save_contact_friend_by') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'SaveContactFriendBy','value'=>@$data['Upline']->SaveContactFriendBy->Type,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_save_contact_friend_interest') ?></label>
                            <div class="col-sm-7">
                            <?php  $interest_list_friend = '';
                                if (is_array(@$data['Upline']->SaveContactFriendBy->InterestId) && count(@$data['Upline']->SaveContactFriendBy->InterestId) > 0){
                                    foreach (@$data['Upline']->SaveContactFriendBy->InterestId as $item) {
                                        $interest_list_friend .= $item.', ';
                                    }
                                }
                            ?>
                            <?= form_input(['name'=>'SaveContactFriendInterest','value'=>$interest_list_friend,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_save_my_contact_by') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'SaveMyContactBy','value'=> @$data['Upline']->SaveMyContactBy->Type,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_save_my_contact_interest') ?></label>
                            <div class="col-sm-7">
                                <?php  $interest_list_me = '';
                                    if(is_array(@$data['Upline']->SaveMyContactBy->InterestId) && count(@$data['Upline']->SaveMyContactBy->InterestId) > 0){
                                        foreach (@$data['Upline']->SaveMyContactBy->InterestId as $item) {
                                            $interest_list_me .= $item.', ';
                                        }
                                    }
                                ?>
                                <?= form_input(['name'=>'SaveMyContactInterest','value'=>@$interest_list_me,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_relationship_status') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'RelationshipStatus','value'=>@$data['Upline']->RelationshipStatus,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_interest') ?></label>
                            <div class="col-sm-7">
                                <?php  $interest_list = '';
                                    if (is_array(@$data['Upline']->InterestId) && count(@$data['Upline']->InterestId) > 0){
                                        foreach (@$data['Upline']->InterestId as $item) {
                                            $interest_list .= $item.', ';
                                        }
                                    }
                                ?>
                                <?= form_input(['name'=>'InterestId','value'=>@$interest_list,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_city') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'CityName','value'=>@$data['Upline']->CityName,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_hoby') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Hoby','value'=>@$data['Upline']->Hoby,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_facebook') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Facebook','value'=>@$data['Upline']->Facebook,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_instagram') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Instagram','value'=>@$data['Upline']->Instagram,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_limit_save_contact_friend_day') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'LimitSaveContactFriendDay','value'=>@$data['Upline']->LimitSaveContactFriendDay,'type'=>'number','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_limit_save_my_contact_day') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'LimitSaveMyContactDay','value'=>@$data['Upline']->LimitSaveMyContactDay,'type'=>'number','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_marketing_code') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'MarketingCode','value'=>@$data['Upline']->MarketingCode,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_product') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Product','value'=>@$data['Upline']->Product,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_website') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Website','value'=>@$data['Upline']->Website,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_username') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'Username','value'=>@$data['Upline']->User->Username,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_allow_share_profile') ?></label>
                            <div class="col-sm-7">
                                <?= form_input(['name'=>'AllowedShareProfile','value'=>(@$data['Upline']->AllowedShareProfile == true ? 'Ya' : 'Tidak'),'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_is_complete') ?></label>
                            <div class="col-sm-7">

                                <?= form_input(['name'=>'IsComplete','value'=>(@$data['Upline']->IsCompleted == true ? 'Lengkap' : 'Tidak Lengkap' ),'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-5 col-form-label"><?= lang('label_register_at') ?></label>
                            <div class="col-sm-7">

                                <?= form_input(['name'=>'IsComplete','value'=>@$data['Upline']->CreatedAt,'type'=>'text','class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                    </div>
                    <!-- /.card-body -->
                    <div class="card-footer">
                    </div>
                    <!-- /.card-footer -->
                <?= form_close() ?>
            </div>
        </div>

        <div class="col-6">
            <?= show_alert() ?>
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><?= lang('label_downline') ?></h3>
                </div>
                <!-- /.card-header -->
                <div class="card-body">
                    <table id="datatables-downline" class="table table-bordered table-hover">
                        <thead>
                            <tr>
                                <th><?= lang('label_nama') ?></th>
                                <th><?= lang('label_gender') ?></th>
                                <th><?= lang('label_email') ?></th>
                                <th><?= lang('label_whatsapp') ?></th>
                                <th><?= lang('label_status') ?></th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>

        <div class="col-6">
            <?= show_alert() ?>
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><?= lang('label_penghasilan') ?></h3>
                </div>
                <!-- /.card-header -->
                <div class="card-body">
                    <table id="datatables-penghasilan" class="table table-bordered table-hover">
                        <thead>
                            <tr>
                                <th><?= lang('label_nama') ?></th>
                                <th><?= lang('label_email') ?></th>
                                <th><?= lang('label_topup') ?></th>
                                <th><?= lang('label_commission') ?></th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
        <div class="col-6">
            <?= show_alert() ?>
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><?= lang('label_contact_save') ?></h3>
                </div>
                <!-- /.card-header -->
                <div class="card-body">
                    <table id="datatables-save" class="table table-bordered table-hover">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th><?= lang('label_nama') ?></th>
                                <th><?= lang('label_email') ?></th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
        <div class="col-6">
            <?= show_alert() ?>
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><?= lang('label_contact_share') ?></h3>
                </div>
                <!-- /.card-header -->
                <div class="card-body">
                    <table id="datatables-share" class="table table-bordered table-hover">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th><?= lang('label_nama') ?></th>
                                <th><?= lang('label_email') ?></th>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</section>
