<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Autosave extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_contact');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_auto_save_contact");
		$this->assets 	= array('assets_index');
        $this->content  = "autosave/index";
        
        $params = [
            'action_home' => 'dashboard',
            'breadcrumb_active' => lang('label_auto_save_contact')
        ];
		$this->template($params);
    }
    public function run(){
        echo json_encode($this->Api_contact->run_cjob_contact_auto_save());
    }
    
}
