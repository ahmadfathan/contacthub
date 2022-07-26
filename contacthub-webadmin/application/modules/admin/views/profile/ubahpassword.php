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
                <h3 class="card-title"><?= lang('label_form_update_password') ?></h3>
              </div>
              <?= form_open_multipart($action_save,array('class'=>'form-horizontal')) ?>
                <form class="form-horizontal">
                    <div class="card-body">
                    <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_last_password') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'LastPassword','type'=>'password','class'=>'form-control','placeholder'=>lang('label_placeholder_last_password'),'required'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_new_password') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'NewPassword','type'=>'password','class'=>'form-control','placeholder'=>lang('label_placeholder_new_password'),'required'=>true]) ?>
                            </div>
                        </div>
                    </div>
                    <!-- /.card-body -->
                    <div class="card-footer">
                        <?= form_button(['type'=>'submit','class'=>'btn btn-info','content'=>lang('label_save')]) ?>
                    </div>
                    <!-- /.card-footer -->
                <?= form_close() ?>
            </div>
        </div>
    </div>
</section>
