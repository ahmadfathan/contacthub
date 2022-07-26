<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Affiliate extends Admin_Controller {

	public function __construct(){
        parent::__construct();
        $this->apiKey = $this->session->userdata($this->_API_KEY);
        $this->load->model('admin/api/Api_affiliate');
		$this->auth();
	}
	public function index()
	{
		$this->title 	= lang("label_affiliate");
		$this->assets 	= array('assets_index');
        $this->content  = "affiliate/index";
        
        $params = [
            'action_home' => 'dashboard',
            'breadcrumb_active' => lang('label_affiliate')
        ];
		$this->template($params);
    }
    
    public function ajax_list(){
        $param = [];
        if ($_POST['search']['value']){
            $param['keyword'] = $_POST['search']['value'];
        }
        $param['numberPage'] = (int) $_POST['length'];
        $param['page'] = (int) $_POST['start'];
        if ($param['page']==0){
            $param['page'] = 1;
        }else{
            $param['page'] = $param['page'] / $param['numberPage'] + 1 ;
        }
        if (isset($_POST['order'])){
            $param['order_column'] = $_POST['order']['0']['column'];
            $param['order_dir'] = $_POST['order']['0']['dir'];
        }
        $list = $this->Api_affiliate->get_datatables_affiliate($param);
        $data = array();
        $no = $_POST['start'];
        $dataTables = array();
        foreach ($list->result->data as $data) {
            $id = $data->_id; // user_id

            $no++;
            $row = array();
            $row[] = $data->Name;
            $row[] = $data->Email;
            $row[] = 'Rp'.number_format($data->Komisi);
            $row[] = $data->Downline;
            $dataTables[] = $row;
        }

        $output = array(
            "draw" => $_POST['draw'],
            "recordsTotal" => $list->result->total,
            "recordsFiltered" => $list->result->total,
            "data" => $dataTables,
        );
        //output to json format
        echo json_encode($output);
    }
    private function labelTable($key){
        switch ($key) {
            case '_id':return lang('label_id');
            case 'Name':return lang('label_nama_role');
            default: return false;
        }
    }
}
