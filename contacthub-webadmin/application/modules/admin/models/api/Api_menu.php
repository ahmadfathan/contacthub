<?php
require APPPATH.'vendor/autoload.php';
use GuzzleHttp\Client;
class Api_menu
{
    public function __construct(){
        $this->app =& get_instance();

        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 120.0,
        ]);

        $base_uri = $this->app->config->item('BASE_URI_API');
        $this->endpoint = [
            'all_visible_position_asc' => 'http://'.$base_uri.'/menu-all',
            'datatables' => 'http://'.$base_uri.'/menu',
            'delete' => 'http://'.$base_uri.'/menu/',
            'get' => 'http://'.$base_uri.'/menu/',
            'update' => 'http://'.$base_uri.'/menu/',
            'insert' => 'http://'.$base_uri.'/menu',
        ];
        $this->requestor = new Requestor(); 
    }
    public function getAllVisible($filter=[]){
        try {
            $response   = $this->client->request('GET', '/menu-all',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => $filter 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function by_id($id){
        try {
            $response   = $this->client->request('GET', '/menu/'.$id);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function get_datatables($filter=[]){
        try {
            $column_order = ['','Kode','Nama'];
            // Response 200 
            if (isset($filter['order_column'])){
                $filter['order_by'] = [
                    $column_order[$filter['order_column']] => $filter['order_dir']
                ];
                unset($filter['order_column']);
                unset($filter['order_dir']);
            }
            $response   = $this->client->request('GET', '/menu',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => $filter 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function insert($data=[]){
        try {
            $response   = $this->client->request('POST', '/menu',[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => $data 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function update($id,$data=[]){
        try {
            $response   = $this->client->request('PUT', '/menu/'.$id,[
                'headers'=> [
                    'Content-Type' => 'application/json'
                ],
                'json' => $data 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
    public function delete($id){
        try {
            $response   = $this->client->request('DELETE', '/menu/'.$id);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
}
