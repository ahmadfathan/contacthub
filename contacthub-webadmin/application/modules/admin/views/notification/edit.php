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
                <h3 class="card-title"><?= lang('label_form_notification') ?></h3>
              </div>
              <?= form_open_multipart($action_save,array('class'=>'form-horizontal')) ?>
                <form class="form-horizontal">
                    <div class="card-body">
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_nama') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Name','type'=>'text','class'=>'form-control','value'=>@$data['Notification']->Name,'placeholder'=>lang('label_placeholder_name_notification')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_title') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Title','type'=>'text','class'=>'form-control','value'=>@$data['Notification']->Title,'placeholder'=>lang('label_placeholder_title')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_body') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Body','type'=>'text','class'=>'form-control','value'=>@$data['Notification']->Body,'placeholder'=>lang('label_placeholder_message')]) ?>
                            </div>
                        </div>
                        
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_type') ?> <b style="color:red">*</b></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('Type',@$data['Type'],@$data['Notification']->Type,array('id'=>'Type', 'class'=>'form-control select2bs4','width'=>'100%','required'=>true));?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_link') ?></label>
                            <div class="col-sm-10">
                                <?= form_input(['name'=>'Link','type'=>'text','value'=>@$data['Notification']->Link,'class'=>'form-control','placeholder'=>lang('label_placeholder_link')]) ?>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-sm-2 col-form-label"><?= lang('label_article') ?></label>
                            <div class="col-sm-10">
                                <?php echo form_dropdown('ArticleId',@$data['Article'],@$data['Notification']->ArticleId,array('id'=>'ArticleId', 'class'=>'form-control select2bs4','width'=>'100%'));?>
                            </div>
                        </div>
                    </div>
                    <!-- /.card-body -->
                    <div class="card-footer">
                        <?= form_button(['name' => 'SaveSend', 'type'=>'submit','class'=>'btn btn-info','content'=>lang('label_save_send')]) ?>
                        <!-- <?= form_button(['name' => 'Send', 'type'=>'submit','class'=>'btn btn-default','content'=>lang('label_send')]) ?> -->
                        <?= anchor($action_cancel,lang('label_cancel'),array('class'=>'btn btn-default float-right')) ?>
                    </div>
                    <!-- /.card-footer -->
                <?= form_close() ?>
            </div>
        </div>
    </div>
</section>
