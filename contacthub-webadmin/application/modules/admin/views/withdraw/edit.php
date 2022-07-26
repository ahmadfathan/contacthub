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
                <h3 class="card-title"><?= lang('label_form_withdraw') ?></h3>
              </div>
              <?= form_open_multipart($action_save,array('class'=>'form-horizontal')) ?>
                <form class="form-horizontal">
                    <div class="card-body">
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_withdraw_bank') ?> </label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'WithdrawBank','type'=>'text','value'=>$data['Credit']->WithdrawBank,'class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_withdraw_norek') ?> </label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'WithdrawBank','type'=>'text','value'=>$data['Credit']->WithdrawNoRek,'class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_withdraw_pemilik_bank') ?> </label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'WithdrawBank','type'=>'text','value'=>$data['Credit']->WithdrawPemilikRek,'class'=>'form-control','disabled'=>true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_status') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                            <?php echo form_dropdown('Status',@$data['Status'],$data['Credit']->Status,array('id'=>'Status', 'class'=>'form-control select2bs4','width'=>'100%','required'=>true));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_amount') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Amount','type'=>'text','value' => $amount,'class'=>'form-control','placeholder'=>lang('label_placeholder_amount'),'required' => true]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_description') ?> </label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Description','type'=>'text','value'=>$data['Credit']->Description,'class'=>'form-control','placeholder'=>lang('label_description')]) ?>
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
