<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Dashboard extends Admin_Controller {

	public function __construct(){
		parent::__construct();
		$this->auth();
		$this->load->model('api/Api_dashboard');
	}
	public function index()
	{
		$this->title 	= "Dasbor";
		$this->assets 	= array('assets_index');
		$this->content  = "dashboard/index";
		$summary = $this->Api_dashboard->get_summary()->result;
		$this->template([
			'Summary' => $summary
		]);
	}
}
