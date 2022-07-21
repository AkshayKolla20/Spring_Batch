package com.projxml.BatchProjXML.Configuration;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Component;

import com.projxml.BatchProjXML.POJO.Employee;

@Component
@Configuration
@EnableBatchProcessing
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class BatchConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private DataSource dataSource;
	
	
	/*
	 * @Bean PUBLICDataSource dataSource() { System.out.println("0"); final
	 * DriverManagerDataSource dataSource = new DriverManagerDataSource();
	 * dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
	 * dataSource.setUrl("jdbc:mysql://localhost:3306/test_db");
	 * dataSource.setUsername("root"); dataSource.setPassword("josh@123");
	 * 
	 * return dataSource; }
	 */
	 
	
	@Bean
	public Job exportUserJob() {
		System.out.println("1");
		return jobBuilderFactory.get("exportUserJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}
	
	@Bean
	public JdbcCursorItemReader<Employee> reader() {
		System.out.println("3");
		JdbcCursorItemReader<Employee> jdbcCursorItemReader = new JdbcCursorItemReader<Employee>();
		jdbcCursorItemReader.setDataSource(dataSource);
		jdbcCursorItemReader.setSql("SELECT emp_id, emp_name, emp_age, dept FROM test_db.employee");
		jdbcCursorItemReader.setRowMapper(new RowMapper<Employee>() {

			@Override
			public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
				Employee emp = new Employee();
				
				emp.setEmpId(rs.getInt("emp_id"));
				emp.setEmpName(rs.getString("emp_name"));
				emp.setEmpAge(rs.getInt("emp_age"));
				emp.setDeptName(rs.getString("dept"));
				
				System.out.println(emp);
				return emp;
			}
		});
		
		return jdbcCursorItemReader;
	}
	
	@Bean
	public StaxEventItemWriter<Employee> writer() {
		System.out.println("4");
		StaxEventItemWriter<Employee> writer = new StaxEventItemWriter<>();
		writer.setResource(new FileSystemResource("C:\\Users\\ue\\Documents\\workspace\\SpringBatchProj\\BatchProjXML\\src\\main\\resources\\Employee.xml"));
		
		Map<String, String> aliases = new HashMap<>();
		aliases.put("employee", "com.projxml.BatchProjXML.POJO.Employee");
		
		XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
		xStreamMarshaller.setAliases(aliases);
		
		writer.setMarshaller(xStreamMarshaller);
		writer.setRootTagName("employee");
		writer.setOverwriteOutput(true);
		
		return writer;
	}
	
	@Bean
	public Step step1() {
		System.out.println("2");
		return stepBuilderFactory.get("step1")
				.<Employee, Employee> chunk(100)
				.reader(reader())
				.writer(writer())
				.build();
	}
	
}
