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
        <div class="col-12">
            <?= show_alert() ?>
            <div class="card">
                <div class="card-header">
                <h3 class="card-title"><?= lang('label_form_customer') ?></h3>
              </div>
              <?= form_open_multipart($action_save,array('class'=>'form-horizontal')) ?>
                <form class="form-horizontal">
                    <div class="card-body">
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_greeting') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('Greeting',$data['Greeting'],@$data['Customer']->Greeting,array('id'=>'Greeting', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_nama') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Name','value'=>@$data['Customer']->Name,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_nama')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_nickname') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Nickname','value'=>@$data['Customer']->User->Nickname,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_nickname')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_whatsapp') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'WhatsApp','value'=>@$data['Customer']->WhatsApp,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_whatsapp')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_address') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Address','value'=>@$data['Customer']->Address,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_address')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_religion') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('Religion',$data['Religion'],@$data['Customer']->Religion,array('id'=>'Religion', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_gender') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('Gender',$data['Gender'],@$data['Customer']->Gender,array('id'=>'Gender', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_business_name') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'BusinessName','value'=>@$data['Customer']->BusinessName,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_business_name')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_email') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Email','value'=>@$data['Customer']->User->Email,'type'=>'email','class'=>'form-control','placeholder'=>lang('label_placeholder_email')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_date_of_birth') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'DateOfBirth','value'=>@$data['Customer']->DateOfBirth,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_date_of_birth')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_business_type') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('BusinessTypeId',$data['BusinessType'],@$data['Customer']->BusinessTypeId,array('id'=>'BusinessName', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_profession') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('ProfessionId',$data['Profession'],@$data['Customer']->ProfessionId,array('id'=>'ProfessionId', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_contact_friend_by') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('SaveContactFriendBy',$data['SaveContactFriendBy'],@$data['Customer']->SaveContactFriendBy->Type,array('id'=>'SaveContactFriendBy', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        
                        <div id="DivSaveContactFriendInterest" class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_contact_friend_interest') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_multiselect('SaveContactFriendInterest[]',$data['Interest'],@$data['Customer']->SaveContactFriendBy->InterestId,array('id'=>'SaveContactFriendInterest', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div id="DivSaveContactFriendCity" class="form-group row" >
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_contact_friend_city') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('SaveContactFriendCityId',$data['City'],@$data['Customer']->SaveContactFriendBy->OtherValue,array('id'=>'SaveContactFriendCityId', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>

                        <div id="DivSaveContactFriendReligion" class="form-group row" >
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_contact_friend_religion') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('SaveContactFriendReligion',$data['Religion'],@$data['Customer']->SaveContactFriendBy->OtherValue,array('id'=>'SaveContactFriendReligion', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div id="DivSaveContactFriendGender" class="form-group row" >
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_contact_friend_gender') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('SaveContactFriendGender',$data['Gender'],@$data['Customer']->SaveContactFriendBy->OtherValue,array('id'=>'SaveContactFriendGender', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_my_contact_by') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('SaveMyContactBy',$data['SaveMyContactBy'],@$data['Customer']->SaveMyContactBy->Type,array('id'=>'SaveMyContactBy', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div id="DivSaveMyContactInterest" class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_my_contact_interest') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_multiselect('SaveMyContactInterest[]',$data['Interest'],@$data['Customer']->SaveMyContactBy->InterestId,array('id'=>'SaveMyContactInterest', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>    
                        <div id="DivSaveMyContactCity" class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_my_contact_city') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('SaveMyContactCityId',$data['City'],@$data['Customer']->SaveMyContactBy->OtherValue,array('id'=>'SaveMyContactCityId', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div id="DivSaveMyContactReligion" class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_my_contact_religion') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('SaveMyContactReligion',$data['Religion'],@$data['Customer']->SaveMyContactBy->OtherValue,array('id'=>'SaveMyContactReligion', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div id="DivSaveMyContactGender" class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_save_my_contact_gender') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('SaveMyContactGender',$data['Gender'],@$data['Customer']->SaveMyContactBy->OtherValue,array('id'=>'SaveMyContactGender', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_relationship_status') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('RelationshipStatus',$data['RelationshipStatus'],@$data['Customer']->RelationshipStatus,array('id'=>'RelationshipStatus', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_interest') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_multiselect('InterestId[]',$data['Interest'],@$data['Customer']->InterestId,array('id'=>'InterestId[]', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_city') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('CityId',@$data['City'],@$data['Customer']->CityId,array('id'=>'CityId', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_hoby') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Hoby','value'=>@$data['Customer']->Hoby,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_hoby')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_facebook') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Facebook','value'=>@$data['Customer']->Facebook,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_facebook')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_instagram') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Instagram','value'=>@$data['Customer']->Instagram,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_instagram')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_limit_save_contact_friend_day') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'LimitSaveContactFriendDay','value'=>@$data['Customer']->LimitSaveContactFriendDay,'type'=>'number','class'=>'form-control','placeholder'=>lang('label_placeholder_limit_save_contact_friend_day')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_limit_save_my_contact_day') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'LimitSaveMyContactDay','value'=>@$data['Customer']->LimitSaveMyContactDay,'type'=>'number','class'=>'form-control','placeholder'=>lang('label_placeholder_limit_save_my_contact_day')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_marketing_code') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'MarketingCode','value'=>@$data['Customer']->MarketingCode,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_marketing_code'),'disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_product') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Product','value'=>@$data['Customer']->Product,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_product')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_website') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Website','value'=>@$data['Customer']->Website,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_website')]) ?>
                            </div>
                        </div>
                        <!-- <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_username') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Username','value'=>@$data['Customer']->User->Username,'type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_username')]) ?>
                            </div>
                        </div> -->
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_password') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Password','type'=>'password','class'=>'form-control','placeholder'=>lang('label_placeholder_password')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_allow_share_profile') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('AllowedShareProfile',$data['AllowedShareProfile'],@$data['Customer']->AllowedShareProfile,array('id'=>'AllowedShareProfile', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_is_complete') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('IsComplete',$data['IsComplete'],@$data['Customer']->IsComplete,array('id'=>'IsComplete', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                    </div>
                    <!-- /.card-body -->
                    <div class="card-footer">
                        <?= form_button(['type'=>'submit','class'=>'btn btn-info','content'=>lang('label_save')]) ?>
                        <?= anchor($action_cancel,lang('label_cancel'),array('class'=>'btn btn-default float-right')) ?>
                    </div>
                    <!-- /.card-footer -->
                <?= form_close() ?>
            </div>
        </div>
    </div>
</section>
