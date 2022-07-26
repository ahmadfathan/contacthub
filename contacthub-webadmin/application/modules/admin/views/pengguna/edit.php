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
                <h3 class="card-title"><?= lang('label_form_pengguna') ?></h3>
              </div>
              <?= form_open_multipart($action_save,array('class'=>'form-horizontal')) ?>
                <form class="form-horizontal">
                    <div class="card-body">
                    <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_username') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Username','type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_username'),'required'=>true,'value'=>@$data['user']->Username]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_nickname') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Nickname','type'=>'text','class'=>'form-control','placeholder'=>lang('label_placeholder_nickname'),'required'=>true,'value'=>@$data['user']->Nickname]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_email') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Email','type'=>'email','class'=>'form-control','placeholder'=>lang('label_placeholder_email'),'required'=>true,'value'=>@$data['user']->Email]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_password') ?></label>
                            <div class="col-sm-9">
                                <?= form_input(['id'=>'password', 'name'=>'Password','type'=>'password','class'=>'form-control','placeholder'=>lang('label_placeholder_password')]) ?>
                            </div>
                            <div class="col-sm-1">
                                <button class="btn btn-default" type="button" id="btn_show_password" title="<?= lang('label_show_password') ?>" data-toggle="tooltip"><i class="fa fa-eye"></i></button>
                                <button class="btn btn-default" type="button" id="btn_generate_password" title="<?= lang('label_generate_password') ?>" data-toggle="tooltip"><i class="fas fa-key"></i></button>
                            </div>
                        </div>

                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_role') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                            <?php echo form_dropdown('RoleId',$data['role'],@$data['user']->RoleId,array('id'=>'RoleId', 'class'=>'form-control select2bs4','width'=>'100%','required'=>true));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_status') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                            <?php echo form_dropdown('Status',$data['status'],@$data['user']->Status,array('id'=>'Status', 'class'=>'form-control select2bs4','width'=>'100%','required'=>true));?>
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
